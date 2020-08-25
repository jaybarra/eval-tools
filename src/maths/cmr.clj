(ns maths.cmr
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.spec.alpha :as spec]
            [clojure.pprint :refer [pprint]]))

;; Specs =============================================================
(spec/def ::env #{:local :sit :uat :prod})

(def scheme-pattern "https?:\\/\\/")
(def cmr-url-pattern "cmr\\.((uat|sit)\\.)?earthdata\\.nasa\\.gov")
(def cmr-local-pattern "localhost:\\d4}")
(def cmr-rx (re-pattern (format "%s(%s|%s)"
                                scheme-pattern
                                cmr-local-pattern
                                cmr-url-pattern)))

(spec/def ::url (spec/and string?
                          #(re-matches cmr-rx %)))

(spec/def ::cmr (spec/keys :req [::env
                                 ::url]))
;; Functions =========================================================
(defn state->cmr-opts
  "Extract CMR connection options from state."
  [state]
  {:post [(spec/valid? ::cmr %)]}
  (get-in state [:connections ::cmr]))

(defn get-collections!
  "GET the collections from the specified CMR collections."
  ([context]
   (get-collections! context nil))
  ([context opts]
   (let [coll-search-url (format "%s/search/collections.json"
                                 (::url (state->cmr-opts context)))
         query-opts {:query-params (merge {:has_granules true}
                                          opts)}]
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
                                 (::url (state->cmr-opts context)))
         query-opts {:query-params
                     (merge {:collection_concept_id coll-id
                             :include_facets "v2"}
                            opts)}]
     (-> coll-search-url
         (client/get query-opts)
         :body
         (json/parse-string true)
         (get-in [:feed :entry])))))

(defn get-granule-v2-facets!
  "GET granule v2 facets for a collection."
  [context coll-id]
  (let [coll-search-url (format "%s/search/granules.json"
                                (::url (state->cmr-opts context)))
        query-opts {:query-params {:collection_concept_id coll-id
                                   :include_facets "v2"
                                   :page_size 100}}]
    (-> coll-search-url
        (client/get query-opts)
        :body
        (json/parse-string true)
        (get-in [:feed :facets]))))

(defn facets-contains-temporal-and-spatial?
  [facets]
  (when-let [cn (seq (:children facets))]
    (->> cn
         (map :title)
         (= ["Temporal" "Spatial"]))))

(defn print-facets-with-temporal-and-spatial!
  "Query CMR for collections and associated granules, print any that 
  contain both Temporal and Spatial"
  [state]
  (let [opts {:page_num 1
              :page_size 100}]
    (->> (get-collections! state opts) 
      (map :id)
      (map (partial get-granule-v2-facets! state))
      flatten
      (filterv facets-contains-temporal-and-spatial?) 
      pprint)))

(def state {:connections
            {::cmr {::env :prod
                    ::url "https://cmr.earthdata.nasa.gov"}}})


