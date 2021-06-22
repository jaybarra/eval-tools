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
   [clojure.string :as str]
   [environ.core :refer [env]]
   [eval.db.event-store :as es]
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

(def encode->json "Encode EDN to JSON"
  (partial muuntaja/encode m "application/json"))

(defn decode-cmr-response-body
  "Decode the body of CMR responses"
  [response]
  (if (get-in response [:headers "Content-Type"])
    (muuntaja/decode-response-body m response)
    (:body response)))

(def umm-json-response->items "Unpack :umm_json format concepts from a response."
  (comp :items decode-cmr-response-body))

(def json-response->entry "Unpack :json format concepts from a response."
  (comp :entry :feed decode-cmr-response-body))
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
    :umm-json})
(spec/def ::cmr-formats cmr-formats)
(spec/def ::concept-type #{:collection :granule :service :tool :concept})
(spec/def ::id (spec/or :id-kw keyword? :id-str string?))
(spec/def ::url string?)
(spec/def ::cmr (spec/keys :req-un [::id ::url]))

(def ^:private keyword->lowercase-str
  (comp str/lower-case name))

(defprotocol CmrClient
  (-invoke [client query] "Send a query to CMR")
  (-echo-token [client] "Return an echo-token"))

(defrecord HttpClient [id url]

  CmrClient

  (-invoke [this query]
    (http/request query))

  (-echo-token [this]
    (->> (:id this)
         keyword->lowercase-str
         (str "cmr-echo-token-")
         keyword
         env)))

(defn create-client
  "Constructs a CMR client.
  An invalid configuration will result in an exception being thrown."
  [cmr-cfg]
  (let [{:keys [id url] :as cfg} cmr-cfg]
    (when-not (spec/valid? ::cmr cmr-cfg)
      (throw (ex-info "Invalid CMR configuration"
                      (spec/explain-data ::cmr cmr-cfg))))
    ;; Return the client
    (->HttpClient id url)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn format->cmr-extension
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
    :umm-json ".umm_json"
    ;; default to empty, CMR will return XML(native) by default
    ""))

(defn invoke
  "Invoke CMR endpoints with a request map and return quit the response.
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
  (let [{root-url :url} client
        token (and (not (:anonymous? opts))
                   (or (:echo-token opts)
                       (-echo-token client)))
        out-request (cond-> request
                      true (assoc :url (str root-url (:url request)))
                      token (assoc-in [:headers "Echo-Token"] token))]
    (log/debug "Sending request to CMR" (:id client) (dissoc request :body))
    (-invoke client out-request)))
