(ns eval.cmr.client
  "Default functionality for interacting with a Common Metadata Repository instance.

  This namespace provides basic interaction with a CMR instance through the [[eval.cmr.client/invoke]] function."
  (:require
   [clj-http.client :as http]
   [clojure.core.async :refer [>! <! <!! go chan promise-chan]]
   [clojure.spec.alpha :as spec]
   [clojure.string :as str]
   [muuntaja.core :as muuntaja]
   [muuntaja.format.core :as fmt-core]
   [muuntaja.format.json :as json-format]
   [taoensso.timbre :as log]))

;; ============================================================================
;; Muuntaja codec
;; ============================================================================
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
  "Muuntaja instance for handling CMR content types. 
   
  This contains decoders for the following format types. Version information 
  may be additionally appended.
  e.g \"Content-Type: application/vnd.nasa.cmr.umm+json;version=1.16.2\"
  
  XML formats:
  * application/dif10+xml
  * application/dif+xml
  * application/echo10+xml
  * application/iso19115+xml
  * application/iso:smap+xml
  
  JSON formats:
  * application/json
  * application/opendata+json
  * application/vnd.nasa.cmr.umm+json
  * application/vnd.nasa.cmr.legacy_umm_results+json"
  (muuntaja/create
   (-> muuntaja/default-options
       (assoc-in
        [:formats "application/extended+json"]
        extended-json-formats)
       (assoc-in
        [:formats "application/extended+xml"]
        extended-xml-formats))))

(defn decode-cmr-response-body
  "Decode the body of CMR responses if Content-Type is set, otherwise return the body raw"
  [response]
  (if (get-in response [:headers "Content-Type"])
    (muuntaja/decode-response-body m response)
    (:body response)))

(def umm-json-response->items
  "Unpack :umm-json format concepts from a response."
  (comp :items decode-cmr-response-body))

(def json-response->entry
  "Unpack :json format concepts from a response."
  (comp :entry :feed decode-cmr-response-body))

;; ============================================================================
;; Specs
;; ============================================================================

(def cmr-formats
  "Set of supported CMR data format keywords."
  #{:atom
    :dif
    :dif10
    :echo10
    :iso
    :iso19115
    :json
    :native
    :opendata
    :umm-json
    :umm-json-legacy
    :xml})

(def cmr-services
  "Set of CMR microservices"
  #{:access-control
    :bootstrap
    :indexer
    :ingest
    :metadata-db
    :legacy
    :search})

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
   :native "application/metadata+xml"
   :opendata "application/opendata+json"
   :umm-json "application/vnd.nasa.cmr.umm+json"
   :umm-json-legacy "application/vnd.nasa.cmr.legacy_umm_results+json"
   :xml "application/xml"})

(defn format->cmr-url-extension
  "CMR supported search extensions"
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
    :umm-json ".umm_json"
    :xml ".xml"
    ""))

(spec/def ::cmr-formats cmr-formats)
(spec/def ::concept-type #{:collection :granule :service :tool :concept})
(spec/def ::url string?)
(spec/def ::endpoints (spec/keys :opt-un [::access-control
                                          ::bootstrap
                                          ::indexer
                                          ::ingest
                                          ::legacy
                                          ::metadata-db
                                          ::search]))
(spec/def ::cmr-cfg (spec/or :url-only (spec/keys :req-un [::url]
                                                  :opt-un [::endpoints])
                             :endpoints-only (spec/keys :req-un [::endpoints]
                                                        :opt-un [::url])
                             :blend (spec/keys :req-un [::url ::endpoints])))
(spec/def ::method #{:get :post :put :delete :option :head})
(spec/def ::body any?)
(spec/def ::headers map?)
(spec/def ::request (spec/keys :req-un [::url ::method]
                               :opt-un [::headers ::body]))
(spec/def ::opts map?)
(spec/def ::category keyword?)
(spec/def ::command (spec/keys :req [::category ::request]))

(defprotocol CmrClient
  (-invoke [client query] "Send a query to CMR")
  (-token [client] "Return the authorization-token associated with this client"))

(defn- redact-headers
  "Redacts query headers for use in logging.
  Can replace sensitive headers such as auth tokens with '[redacted]'"
  [query redacting-headers]
  (loop [updated-query query
         headers redacting-headers]
    (if-let [header (first headers)]
      (recur
       (if (get-in query [:headers (first headers)])
         (assoc-in query [:headers header] "[redacted]")
         updated-query)
       (rest headers))
      updated-query)))

(defn ^:private handle-cmr-response
  "Do any formatting by content-type here."
  [resp]
  (try
    (if-let [fault (:eval.cmr.client/category resp)]
      fault
      resp)
    (catch Exception t
      {:eval.cmr.client/category :eval.anomalies/fault
       ::exception t})))

(defn ^:private send-request
  "Send a query to CMR and and recieve a response in a promise-chan."
  [query]
  (let [response-ch (chan 1)
        result-ch (promise-chan)]
    (log/info "Sending request to CMR"
              (redact-headers query [:authorization
                                     :echo-token]))
    ;; Send the request and write the response to an internal chan
    (go (>! response-ch (try
                          (http/request query)
                          (catch Exception t
                            {:eval.cmr.client/category :eval.anomalies.fault
                             ::exception t}))))
    ;; Wait for the response to come back on a separate thread
    (go
      (let [response (<! response-ch)]
        (>! result-ch (handle-cmr-response response))))
    ;; return the result chan containing the parsed response
    (<!! result-ch)))

(defrecord HttpClient [url token endpoints]

  CmrClient

  (-invoke
    [_ command]
    (log/info "Invoking CMR" (dissoc command ::request))
    (send-request (::request command)))

  (-token
    [this]
    (get-in this [:token])))

(defn create-client
  "Constructs a CMR client."
  [cmr-cfg]
  (when-not (spec/valid? ::cmr-cfg cmr-cfg)
    (throw (ex-info "Invalid CMR configuration"
                    (spec/explain-data ::cmr-cfg cmr-cfg))))
  (let [{:keys [url token endpoints]} cmr-cfg]
    (->HttpClient url token endpoints)))

(defn ^:private replace-service-route
  "Replaces the service route from the command URL with the new path."
  [command new-route]
  (update-in command
             [::request :url]
             #(as-> % s
                (str/split s #"/")
                (drop 2 s)
                (str/join "/" s)
                (str new-route
                     (when-not (str/ends-with? new-route "/") "/")
                     s))))

;; ============================================================================
;; Functions
;; ============================================================================

(defn invoke
  "Invoke CMR endpoints with a request map and return quit the response.
  Throws with exceptional response status (>= status 400)

  The [[CmrClient]] url will be prefixed to the provided url to determine
  where the request should be sent.

  Sends a query to CMR over HTTP and returns the response object.
  
  If an authorization-token is available for the provided [[CmrClient]], the token
  will be added to the \"Authorization\" header. This may be ignored by setting
  :anonymous? to true in the options.

  ## Options
  |option| type | description|
  |------|------|------------|
  |`:anonymous?`| optional boolean| When true, no authorization-token will be added to the header |
  |`:token`| optional string | Authorization token to be used. Should be in the form of \"Bearer EDL-xxxxxx...\"<br>See https://urs.earthdata.nasa.gov for Earthdata Login token generation|"
  [client command]
  (when-not (spec/valid? ::command command)
    (throw (ex-info "Invalid CMR command"
                    (spec/explain-data ::command command))))
  (let [{:keys [anonymous? token]} (:opts command)
        {root-url :url
         endpoints :endpoints}   client
        req-url (get-in command [::request :url])

        ;; check if overriding the root-url
        override-url (when endpoints
                       (get endpoints (keyword (second (str/split req-url #"/")))))
        root-url (or override-url root-url)

        ;; if overriding the default endpoint also trim the request url
        command (if override-url
                  (replace-service-route command override-url)
                  (update-in command [::request :url] #(str root-url %)))

        token (and (not anonymous?)
                   (or token (-token client)))

        command (if token
                  (assoc-in command [::request :headers :authorization] token)
                  command)]
    (-invoke client command)))

(comment
  (def client (create-client {:url "https://cmr.earthdata.nasa.gov"}))
  (require '[eval.cmr.commands.search :refer [search]])
  (invoke client (search :collection {:provider "NSIDC_ECS"} {:format :json})))
