(ns eval.cmr.core
  (:require
   [cheshire.core :as json]
   [clj-http.client :as client]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [environ.core :refer [env]]
   [taoensso.timbre :as log]))

;; Specs =============================================================
(spec/def ::env #{:local :sit :uat :prod})

(def scheme-pattern "https?:\\/\\/")
(def cmr-url-pattern "cmr\\.((uat|sit)\\.)?earthdata\\.nasa\\.gov")
(def cmr-local-pattern "localhost:\\d{4}")
(def cmr-rx (re-pattern (format "%s(%s|%s)"
                                scheme-pattern
                                cmr-local-pattern
                                cmr-url-pattern)))

(spec/def ::url (spec/and string?
                          #(re-matches cmr-rx %)))

(spec/def ::cmr (spec/keys :req [::env
                                 ::url]))
;; Functions =========================================================
(defn state->cmr
  "Extract CMR connection options from state."
  [state]
  {:post [(spec/valid? ::cmr %)]}
  (get-in state [:connections ::cmr]))

(defn get-echo-token
  [cmr-env]
  (env (str "CMR_ECHO_TOKEN_" (string/upper-case (name cmr-env)))))

(defn get-collections!
  "GET the collections from the specified CMR collections."
  ([context]
   (get-collections! context nil))
  ([context opts]
   (let [coll-search-url (format "%s/search/collections.json"
                                 (::url (state->cmr context)))
         echo-token (get-echo-token (::env (state->cmr context)))
         query-opts {:query-params opts
                     :headers {"Echo-Token" echo-token}}]
     (-> coll-search-url
         (client/get query-opts)
         :body
         (json/parse-string true)
         (get-in [:feed :entry])))))

(defn get-granules!
  "GET the granules from the specified CMR collections for a given
  collection."
  ([context coll-id]
   (get-granules! context coll-id nil))
  ([context coll-id opts]
   (let [coll-search-url (format "%s/search/granules.json"
                                 (::url (state->cmr context)))
         echo-token (get-echo-token (::env (state->cmr context)))
         query-opts {:query-params
                     (merge {:collection_concept_id coll-id}
                            opts)
                     :headers {"Echo-Token" echo-token}}]
     (-> coll-search-url
         (client/get query-opts)
         :body
         (json/parse-string true)
         (get-in [:feed :entry])))))

(defn get-granule-v2-facets!
  "GET granule v2 facets for a collection."
  [context coll-id]
  (let [coll-search-url (format "%s/search/granules.json"
                                (::url (state->cmr context)))
        query-opts {:query-params {:collection_concept_id coll-id
                                   :include_facets "v2"
                                   :page_size 100}}]
    (-> coll-search-url
        (client/get query-opts)
        :body
        (json/parse-string true)
        (get-in [:feed :facets]))))

(defn facets-contains-temporal-and-spatial?
  "Return true if a faceted query result contains temporal and spatial
  facets."
  [facets]
  (when-let [cn (seq (:children facets))]
    (->> cn
         (map :title)
         (= ["Temporal" "Spatial"]))))

(defn get-facets-with-temporal-and-spatial!
  "Query CMR for collections and associated granules, print any that 
  contain both Temporal and Spatial"
  ([state]
   (get-facets-with-temporal-and-spatial! state nil))
  ([state m-opts]
   (let [opts (merge {:page_num 1
                      :page_size 500
                      :has_granules true}
                     m-opts)
         fetch-collections! (partial get-collections! state)
         fetch-coll-granules! (partial get-granule-v2-facets! state)]
     (log/debug "Fetching facets" opts)
     (->> (fetch-collections! opts)
          (map :id)
          (pmap fetch-coll-granules!)
          (filterv facets-contains-temporal-and-spatial?)))))

(defn get-collections-with-temporal-and-spatial!
  ([state]
   (get-collections-with-temporal-and-spatial! state nil))
  ([state m-opts]
   (let [opts (merge {:page_num 1
                      :page_size 500
                      :has_granules true}
                     m-opts)
         fetch-collections! (partial get-collections! state)
         fetch-coll-granules! (partial get-granule-v2-facets! state)]
     (log/debug "Fetching collections" opts)
     (->> (fetch-collections! opts)
          (pmap #(merge % {:granules (fetch-coll-granules! (:id %))}))
          (filterv #(facets-contains-temporal-and-spatial? (:granules %)))))))

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


