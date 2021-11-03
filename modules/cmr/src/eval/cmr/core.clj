(ns eval.cmr.core
  "Default functionality for interacting with a Common Metadata Repository instance.

  This namespace provides basic interaction with a CMR instance through the [[eval.cmr.core/invoke]] function."
  (:require
   [clj-http.client :as http]
   [clojure.spec.alpha :as spec]
   [clojure.string :as str]
   [muuntaja.core :as muuntaja]
   [muuntaja.format.json :as json-format]
   [muuntaja.format.core :as fmt-core]
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

(def encode->json "Encode EDN to JSON"
  (partial muuntaja/encode m "application/json"))

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
(spec/def ::endpoints map?)
(spec/def ::cmr (spec/keys :req-un [::url]))

(spec/def ::method #{:get :post :put :delete :option :head})
(spec/def ::body any?)
(spec/def ::headers map?)
(spec/def ::request (spec/keys :req-un [::url ::method]
                               :opt-un [::headers ::body]))
(spec/def ::opts map?)
(spec/def ::command (spec/keys :req-un [::request]
                               :opt-un [::opts]))

(defprotocol CmrClient
  (-invoke [client query] "Send a query to CMR")
  (-token [client] "Return the authorization-token associated with this client"))

(defn- redact-headers
  "Redacts query headers for use in logging.
  Can replace sensitive headers such as auth tokens with 'redacted'"
  [query headers]
  (loop [updated-query query 
         header (first headers)]
    (if-not header
      updated-query
      (recur
       (if (get-in query [:headers (first headers)])
         (update-in query [:headers header] (constantly "[redacted]"))
         query)
       (rest headers)))))

(defrecord HttpClient [url opts]

  CmrClient

  (-invoke [this query]
    (log/info (format "Sending request to CMR [%s]" (get this :url))
              (redact-headers query [:authorization
                                     :echo-token]))
    (http/request query))

  (-token [this]
    (get-in this [:opts :token])))

(defn create-client
  "Constructs a CMR client."
  [cmr-cfg]
  (let [{:keys [url]} cmr-cfg]
    (when-not (spec/valid? ::cmr cmr-cfg)
      (throw (ex-info "Invalid CMR configuration"
                      (spec/explain-data ::cmr cmr-cfg))))
    (->HttpClient url (dissoc cmr-cfg :url))))

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
  |`:token`| optional string | Authorization token to be used|"
  [client command]
  (when-not (spec/valid? ::command command)
    (throw (ex-info "Invalid CMR command"
                    (spec/explain-data ::command command))))
  (let [{:keys [anonymous? token]} (:opts command)
        {root-url :url
         {endpoints :endpoints} :opts} client
        req-url (get-in command [:request :url])

        ;; check if overriding the root-url
        override-url (when endpoints
                       (get endpoints (keyword (second (str/split req-url #"/")))))
        root-url (or override-url root-url)

        ;; if overriding the default endpoint also trim the request url
        command (if override-url
                  (update-in command
                             [:request :url]
                             #(as-> % s
                                (str/split s #"/")
                                (drop 2 s)
                                (str/join "/" s)
                                (str "/" s)))
                  command)

        token (and (not anonymous?)
                   (or token (-token client)))
        out-request (cond-> (:request command)
                      true (assoc :url (str root-url (get-in command [:request :url])))
                      token (assoc-in [:headers :authorization] token))]
    (-invoke client out-request)))

(comment
  (def c (create-client {:url "http://localhost"
                         :endpoints {:search "http://localhost:3003"}}))
  (require '[eval.cmr.commands.search :refer [search]])
  (invoke c (search :collection "PROV")))
