(ns eval.cmr.core
  (:require
   [clj-http.client :as client]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [environ.core :refer [env]]
   [muuntaja.core :as m]
   [taoensso.timbre :as log])
  (:import
   [clojure.lang ExceptionInfo]))

(def m (m/create
        (assoc-in m/default-options
                  [:formats "application/json" :matches]
                  #"^application/vnd\.nasa\.cmr\.umm_results\+json.*")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::env #{:local :sit :uat :prod})
(spec/def ::concept-type #{:collection :granule})

(def scheme-pattern "https?:\\/\\/")
(def cmr-url-pattern "cmr\\.((uat|sit)\\.)?earthdata\\.nasa\\.gov")
(def cmr-local-pattern "localhost:\\d{4}")
(def cmr-rx (re-pattern (format "%s(%s|%s)"
                                scheme-pattern
                                cmr-local-pattern
                                cmr-url-pattern)))

(spec/def :v2-facets/title string?)
(spec/def :v2-facets/facet (spec/keys :req-un [:v2-facets/title]))
(spec/def :v2-facets/children (spec/* :v2-facets/facet))
(spec/def :v2-facets/facets (spec/keys :req-un [:v2-facets/title]
                                       :opt-un [:v2-facets/children]))


(spec/def ::url (spec/and string?
                          #(re-matches cmr-rx %)))

(spec/def ::cmr (spec/keys :req [::env
                                 ::url]))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn cmr
  "Return a CMR connection object."
  [cmr-env]
  {:post [(spec/valid? ::cmr %)]}
  (case cmr-env
    :sit {::env cmr-env
          ::url "https://cmr.sit.earthdata.nasa.gov"}
    :uat {::env cmr-env
          ::url "https://cmr.uat.earthdata.nasa.gov"}
    :prod {::env cmr-env
           ::url "https://cmr.earthdata.nasa.gov"}
    ;; default
    {::env :local
     ::url "http://localhost:3003"}))

(defn cmr-state
  "Return a CMR enabled state"
  ([cmr-env]
   (cmr-state {} cmr-env))
  ([state cmr-env]
   (let [cmr-opts (cmr cmr-env)
         cons (merge (:connections state)
                     {::cmr cmr-opts})]
     (merge state {:connections cons }))))

(defn state->cmr
  "Extract CMR connection options from state."
  [state]
  {:post [(spec/valid? ::cmr %)]}
  (get-in state [:connections ::cmr]))

(defn get-echo-token
  [cmr-env]
  (env (str "CMR_ECHO_TOKEN_" (string/upper-case (name cmr-env)))))

(defn cmr-hits
  "Query CMR for count of available concepts."
  ([state concept-type]
   (cmr-hits state concept-type nil))
  ([state concept-type m-opts]
   {:pre [(spec/valid? ::concept-type concept-type)]}
   (let [{cmr-url ::url
          cmr-env ::env} (state->cmr state)
         target-url (format "%s/search/%ss"
                            cmr-url
                            (name concept-type))
         query-params (merge {:page_size 0} m-opts)
         opts {:query-params query-params
               :headers {"Echo-Token" (get-echo-token cmr-env)}
               :cookie-policy :standard}]
     (try 
       (-> (client/get target-url opts)
           (get-in [:headers "CMR-Hits"] 0)
           (Integer/parseInt))
       (catch ExceptionInfo e
         (log/error e)
         0)))))

(defn cmr-granule-hits
  "Query CMR for count of available granules in a collection."
  [state coll-id]
  (cmr-hits state :granule {:collection_concept_id coll-id}))

(defn get-collections
  "GET the collections from the specified CMR collections."
  ([state]
   (get-collections state nil))
  ([state opts]
   (let [{cmr-url ::url
          cmr-env ::env} (state->cmr state)
         coll-search-url (format "%s/search/collections.umm_json" cmr-url)
         echo-token (get-echo-token cmr-env)
         query-opts {:query-params opts
                     :cookie-policy :standard
                     :headers {"Echo-Token" echo-token}}]
     (-> coll-search-url
         (client/get query-opts)
         m/decode-response-body
         (get-in [:feed :entry])))))

(defn get-granules
  "GET the granules from the specified CMR collections for a given
  collection."
  ([state coll-id]
   (get-granules state coll-id nil))
  ([state coll-id m-opts]
   (let [{cmr-url ::url
          cmr-env ::env} (state->cmr state)
         coll-search-url (format "%s/search/granules.json" cmr-url)
         echo-token (get-echo-token cmr-env)
         query-opts {:query-params
                     (merge {:collection_concept_id coll-id}
                            m-opts)
                     :headers {"Echo-Token" echo-token}
                     :cookie-policy :standard}]
     (-> coll-search-url
         (client/get query-opts)
         m/decode-response-body
         (get-in [:feed :entry])))))

(defn get-granule-v2-facets
  "GET granule v2 facets for a collection, ignoring granules."
  [state coll-id]
  (let [{cmr-url ::url
         cmr-env ::env} (state->cmr state)
        coll-search-url (format "%s/search/granules.json" cmr-url)
        query-opts {:query-params {:collection_concept_id coll-id
                                   :page_size 0
                                   :include_facets "v2"}
                    :headers {"Echo-Token" (get-echo-token cmr-env)}
                    :cookie-policy :standard}]
    (log/debug "Fetching granules with v2 facets for collection"
               coll-id)
    (-> coll-search-url
        (client/get query-opts)
        m/decode-response-body
        (get-in [:feed :facets]))))

(defn facets-contains-type?
  "Return true if a faceted query result contains a facet with the
  given name."
  [type facets]
  {:pre [(spec/valid? :v2-facets/facets facets)]}
  (when-let [nodes (seq (:children facets))]
    (->> nodes
         (map :title)
         (some #{type})
         some?)))

(defn get-facets-with-temporal-and-spatial
  "Query CMR for collections and associated granules, print any that 
  contain both Temporal and Spatial"
  ([state]
   (get-facets-with-temporal-and-spatial state nil))
  ([state m-opts]
   (let [opts (merge {:page_num 1
                      :has_granules true}
                     m-opts)
         fetch-collections! (partial get-collections state)
         fetch-coll-granules! (partial get-granule-v2-facets state)
         contains-spatial? (partial facets-contains-type? "Spatial")
         contains-temporal? (partial facets-contains-type? "Temporal")]
     (log/debug "Fetching facets" opts)
     (->> (fetch-collections! opts)
          (map :id)
          (pmap fetch-coll-granules!)
          (filterv contains-spatial?)
          (filterv contains-temporal?)))))

(defn get-collections-with-temporal-and-spatial
  ([state]
   (get-collections-with-temporal-and-spatial state nil))
  ([state m-opts]
   (let [opts (merge {:has_granules true}
                     m-opts)
         fetch-collections! (partial get-collections state)
         fetch-coll-granules! (partial get-granule-v2-facets state)
         contains-spatial? (fn [c-with-g]
                             (facets-contains-type?
                              "Spatial"
                              (:granules c-with-g)))
         contains-temporal? (fn [c-with-g]
                              (facets-contains-type?
                               "Temporal"
                               (:granules c-with-g)))]
     (log/debug "Fetching collections" opts)
     (->> (fetch-collections! opts)
          (pmap #(merge % {:granules (fetch-coll-granules! (:id %))}))
          (filterv contains-spatial?)
          (filterv contains-temporal?)))))

(defn find-all-collections-with-spatial-and-temporal
  "Crawls through CMR to find any collection with facets that have
  temporal and spatial facets."
  [state]
  (let [n-coll (cmr-hits state :collection {:has_granules true})
        page-size 100
        max-page (int (Math/ceil (/ n-coll page-size)))]
    (loop [page-num 1
           colls []]
      (if (< max-page page-num)
        colls
        (do
          (log/debug (format "Searching page %d of %d"
                             page-num
                             max-page))
          (recur (inc page-num)
                 (if-let [results (seq (get-collections-with-temporal-and-spatial
                                        state
                                        {:page_num page-num
                                         :page_size page-size}))]
                   (into colls (map #(select-keys % [:id]) results))
                   colls)))))))

(defn ingest-collection
  "Send a collection to CMR to be ingested."
  ([state provider-id collection]
   (ingest-collection state provider-id collection nil))
  ([state provider-id collection opts]
   (log/info (format "Ingesting collection [%s] on provider [%s]"
                     (:native-id collection)
                     provider-id))
   (let [{cmr-url ::url
          cmr-env ::env} (state->cmr state)
         url (format "%s/search/providers/%s/collections/%s"
                     cmr-url
                     provider-id
                     (:native-id collection))]
     (client/put url {:body (m/encode "application/json" collection)
                      :headers {"Echo-Token" (get-echo-token cmr-env)
                                "Content-Type" "application/umm+json"}
                      :cookie-policy :standard}))))
