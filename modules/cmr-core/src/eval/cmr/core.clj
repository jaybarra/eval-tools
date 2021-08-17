(ns eval.cmr.core
  "Default functionality for interacting with a Common Metadata Repository instance.

  This namespace provides basic interaction with a CMR instance through the [[invoke]] function."
  (:require
   [clj-http.client :as http]
   [clojure.spec.alpha :as spec]
   [clojure.string :as str]
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

(spec/def ::method #{:get :post :put :delete :option :head})
(spec/def ::body any?)
(spec/def ::headers map?)
(spec/def ::request (spec/keys :req-un [::url ::method]
                               :opt-un [::headers ::body]))
(spec/def ::opts map?)
(spec/def ::command (spec/keys :req-un [::request]
                               :opt-un [::opts]))

(def ^:private keyword->lowercase-str
  (comp str/lower-case name))

(defprotocol CmrClient
  (-invoke [client query] "Send a query to CMR")
  (-echo-token [client] "Return the echo-token associated with this client"))

(defrecord HttpClient [id url opts]

  CmrClient

  (-invoke [this query]
    (letfn [(obfuscate-token [q]
              (if (string? (get-in q [:headers "Echo-Token"]))
                (update-in q [:headers "Echo-Token"] #(str (subs % 0 8) "-XXXX-XXXX-XXXX-XXXXXXXXXXXX"))
                q))]
      (log/debug (format"Sending request to CMR [%s]" (get this :url)) (obfuscate-token query))
      (http/request query)))

  (-echo-token [this]
    (get-in this [:opts :echo-token])))

(defn create-client
  "Constructs a CMR client.
  An invalid configuration will result in an exception being thrown."
  [cmr-cfg]
  (let [{:keys [id url] :as cfg} cmr-cfg]
    (when-not (spec/valid? ::cmr cmr-cfg)
      (throw (ex-info "Invalid CMR configuration"
                      (spec/explain-data ::cmr cmr-cfg))))
    ;; Return the client
    (->HttpClient id url (dissoc cmr-cfg :url :id))))

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
   :umm-json "application/vnd.nasa.cmr.umm+json"})

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
    :xml ".xml"
    ;; default to empty, CMR will return default for the endpoint
    ""))

(defn- encode-req-body
  "Determines if the body needs to be encoded and updates the :body of the request.
  Uses the [[cmr-formats]] to determine encoding"
  [req fmt]
  (letfn [(process-body [data]
            (if (some #{:umm-json :json} [fmt])
              (encode->json data)
              data))]
    (update req :body process-body)))

(defn invoke
  "Invoke CMR endpoints with a request map and return quit the response.
  Throws with exceptional response status (>= status 400)

  The [[CmrClient]] url will be prefixed to the provided url to determine
  where the request should be sent.

  Sends a query to CMR over HTTP and returns the response object.
  
  If an echo-token is available for the provided [[CmrClient]], the token
  will be added to the \"Echo-Token\" header. This may be ignored by setting
  :anonymous? to true in the options.

  ## Options
  :anonymous? optional boolean - when true, no echo-token will be added to the header
  :echo-token optional string  - when set, will be used unless :anonymous? is true "
  [client command]
  (when-not (spec/valid? ::command command)
    (throw (ex-info "Invalid CMR command"
                    (spec/explain-data ::command command))))
  (let [{:keys [anonymous? echo-token]} (:opts command)
        {root-url :url
         {endpoints :endpoints} :opts} client
        req-url (get-in command [:request :url])

        ;; check if overriding the root-url
        override-url (when endpoints
                       (get endpoints (keyword (second (str/split req-url #"/")))))
        root-url (or override-url root-url)

        ;; if overriding the defaul endpoint also trim the request url
        command (if override-url
                  (update-in command [:request :url] #(as-> % s
                                                        (str/split s #"/")
                                                        (drop 2 s)
                                                        (str/join "/" s)
                                                        (str "/" s)))
                  command)
        
        token (and (not anonymous?)
                   (or echo-token (-echo-token client)))
        out-request (cond-> (:request command)
                      true (assoc :url (str root-url (get-in command [:request :url])))
                      token (assoc-in [:headers "Echo-Token"] token))]
    (-invoke client out-request)))
