(ns eval.cmr.core
  "Default functionality for interacting with a Common Metadata Repository
  instance.

  This namespace provides basic interaction through the [[invoke!]]
  function.

  See Also:
  * [[eval.cmr.bulk.granule]]"
  (:require
   [clj-http.client :as http]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [environ.core :refer [env]]
   [eval.db.event-store :as es]
   [eval.system :as system]
   [eval.utils.core :refer [defn-timed]]
   [muuntaja.core :as muuntaja]
   [muuntaja.format.json :as json-format]
   [muuntaja.format.core :as fmt-core]
   [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Muuntaja codec
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def extended-json-formats
  "Let the decoder know how to decode UMM JSON"
  {:decoder [json-format/decoder {:decode-key-fn true}]
   :matches #"^application/(.*)\+json.*"})

(def extended-xml-formats
  "Let the decoder know how to decode XML formats, echo10, iso:smap..."
  {:decoder (reify
              fmt-core/Decode
              (decode [_ xml _charset]
                (slurp xml)))
   :matches #"^application/((.*)\+)?xml.*"})

(def m
  "Muuntaja instance for handling CMR content types. This contains
  decoders for the following format types. Version information may be
  additionally appended.

  e.g \"Content-Type: application/vnd.nasa.cmr.umm+json;version=1.16.2\"
  
  XML formats:
  * application/dif10+xml
  * application/dif+xml
  * application/echo10+xml
  * application/iso19115+xml
  * application/iso:smap+xml
  
  JSON formats:
  * application/vnd.nasa.cmr.umm+json"
  (muuntaja/create
   (-> muuntaja/default-options
       (assoc-in 
        [:formats "application/extended+json"]
        extended-json-formats)
       (assoc-in 
        [:formats "application/extended+xml"]
        extended-xml-formats))))

(def encode->json (partial muuntaja/encode m "application/json"))

(def decode-cmr-response "Decode the body of CMR responses"
  (partial muuntaja/decode-response-body m))

(def umm-json-response->items "Unpack :umm_json format concepts from a response."
  (comp :items decode-cmr-response))

(def json-response->entry "Unpack :json format concepts from a response."
  (comp :entry :feed decode-cmr-response))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def cmr-formats "Set of supported CMR data format keywords."
  #{:atom
    :dif 
    :dif10 
    :echo10
    :iso 
    :iso19115
    :json 
    :xml
    :native
    :umm_json})
(spec/def ::cmr-formats cmr-formats)
(spec/def ::concept-type #{:collection :granule :service :tool :concept})
(spec/def ::id (spec/or :id-kw keyword? :id-str string?))
(spec/def ::url string?)
(spec/def ::cmr (spec/keys :req [::id ::url]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- -load-client
  "Read CMR instance from config and return the config"
  [cmr]
  (if-let [cmr-instance (system/config :cmr :instances cmr)]
    (let [url (if (map? cmr-instance)
                (:url cmr-instance)
                cmr-instance)]
      {::id cmr ::url url})
    (throw (ex-info "No entry found in configuration for specified CMR instance"
                    {:cmr cmr}))))
(defn client
  "Create a CMR client
  
  When given a keyword it will look up the from the system configuration
  When given a map it will attempt to contruct the client

  Configure environments in the configuration under :cmr :cmr-instances"
  [cmr]
  (let [client (if (map? cmr)
                 cmr
                 (-load-client cmr))]
    (if (spec/valid? ::cmr client)
      client
      (throw (ex-info "Invalid CMR configuration"
                      (spec/explain-data ::cmr client))))))

(def ^:private keyword->lowercase-str
  (comp string/lower-case name))

(defn echo-token
  "Read CMR enviroment echo token from environment variables.
  By default it reads from CMR_ECHO_TOKEN_*
  where the * is the matching keyword name of the provided cmr-env

  e.g.
  `(echo-token :prod) will look for an environment variable CMR_ECHO_TOKEN_PROD
  `(echo-token :uat)  will look for an environment variable CMR_ECHO_TOKEN_UAT

  TODO: get echo-tokens from configs or dynamically in the library"
  [client]
  (->> client
       ::id
       keyword->lowercase-str
       (str "cmr-echo-token-")
       keyword
       env))

(def format->mime-type
  "CMR support format to MIME type map."
  {:atom "application/atom+xml"
   :dif "application/dif+xml"
   :dif10 "application/dif10+xml"
   :echo10 "application/echo10+xml"
   :html "text/html"
   :iso "application/iso19115+xml"
   :iso19115 "application/iso19115+xml"
   :json "application/json"
   :xml "application/xml"
   :native "application/metadata+xml"
   :umm_json "application/vnd.nasa.cmr.umm+json"})

(defn- format->cmr-extension
  "Takes a CMR supported format and returns the appropriate extension
  for use on the search endpoint

  e.g. {:format :echo10} will yield \"/search/collections.echo10\"
  after being passed through [[search]]"
  [fmt]
  (case fmt
    :atom ".atom"
    :dif ".dif"
    :dif10 ".dif10"
    :echo10 ".echo10"
    :iso ".iso"
    :iso19115 ".iso19115"
    :json ".json"
    :native ".native"
    :umm_json ".umm_json"
    ;; default to empty, CMR will return XML(native) by default
    ""))

(defn-timed invoke
  "Invoke CMR endpoints with a request map and return the response.
  Throws with exceptional response status (>= status 400)

  The [[client]] url will be prefixed to the provided url to determine
  where the request should be sent.

  Sends a query to CMR over HTTP and returns the response object.
  
  If an echo-token is available for the provided [[client]] it will
  be added to the \"Echo-Token\" header. This may be ignored by setting
  :anonymous? to true in the options.

  ## Options
  :anonymous? boolean - when true, no echo-token will be added to the header
  :echo-token string  - when set, will be used unless :anonymous? is true

  TODO: make an async version of this"
  [client request & [opts]]
  (let [{cmr-url ::url cmr-name ::name} client
        token (and (not (:anonymous? opts))
                   (or (:echo-token opts)
                       (echo-token client)))
        out-request (cond-> request
                      ;; prefix with the CMR instance
                      true (assoc :url (str cmr-url (:url request)))
                      ;; Insert echo-token if available
                      token (assoc-in [:headers "Echo-Token"] token))]
    (log/debug "Sending request to CMR" client (dissoc request :body))
    (http/request out-request)))

(defn search-request
  "GET the collections from the specified CMR enviroment.

  Send a GET request to the search endpoint for the specific concept-type
  as a query-param."
  ([concept-type query & [opts]]
   (let [search-url (format
                     "/search/%ss%s"
                     (name concept-type)
                     (format->cmr-extension (:format opts)))]
     {:method :get
      :url search-url
      :query-params query})))

(defn clear-scroll-session!
  "Clear the CMR scroll session."
  [client scroll-id]
  (let [request {:method :post
                 :url "/search/clear-scroll"
                 :headers {:content-type "application/json"}
                 :body (encode->json {:scroll_id scroll-id})}]
    (log/debug "Clearing scroll session [" scroll-id "]")
    (invoke client request)))

(defn search
  "Return a response from CMR."
  [client concept-type query & [opts]]
  (invoke client
          (search-request concept-type query opts)
          opts))

(defn scroll!
  "Begin or continue a scrolling session and returns a map with 
  :CMR-Scroll-Id and :response.

  ## CMR-Scroll-Id

  The first scroll! query will return the CMR-Scroll-Id in the header
  of the response. Add this to the options map of subsequent calls to
  continue getting results.

  e.g.
  %> (scroll! cmr :granules query)
  {:CMR-Scroll-Id \"612341\"
   :response <page 1 of results>}

  %> (scroll! cmr :granules query {:CMR-Scroll-Id \"612341\"})
  {:CMR-Scroll-Id \"612341\"
   :response <page 2 of results>}

  %> (get-in (clear-scroll-session! cmr \"612341\") :status)
  204

  The ideal use case is to always run in a try-finally
  e.g.

  (try
    (scroll! cmr query {:CMR-Scroll-id scroll-id)
    (finally (clear-scroll-session! cmr scroll-id))
  
  ## Query Parameters
  Standard [[search]] parameters are accepted with the following exceptions.
  
  :page_num and :offset are not valid params when using the scrolling endpoint.
  :page_size is a valid query param and must be below 2000

  Repeated calls will yield additional results.

  Be sure to call [[clear-scroll-session!]] when finished. "
  [client concept-type query & [opts]]
  (let [scroll-query (-> query
                         (dissoc :page_num :offset)
                         (assoc :scroll true))
        existing-scroll-id (:CMR-Scroll-Id opts)
        request (search-request concept-type scroll-query opts)
        scroll-request (cond-> request
                         existing-scroll-id (assoc-in
                                             [:headers :CMR-Scroll-Id]
                                             existing-scroll-id))
        response (invoke scroll-request opts)
        scroll-id (get-in response [:headers :CMR-Scroll-Id])]
    (if existing-scroll-id
      (log/debug "Continuing scroll [" scroll-id "]")
      (log/debug "Started new scroll session [" scroll-id "]"))
    {:CMR-Scroll-Id scroll-id
     :response response}))

(defn query-hits
  "Query CMR for count of available concepts that are available from
  a given query.

  Takes a query and sets a :page_size of 0 and returns
  the CMR-Hits header string as an integer value."
  [client concept-type query & [opts]]
  (let [query (assoc query :page_size 0)]
    (-> (search client concept-type query)
        (get-in [:headers :CMR-Hits])
        Integer/parseInt)))
