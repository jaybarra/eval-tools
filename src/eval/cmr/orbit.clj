(ns eval.cmr.orbit
  (:require
   [eval.cmr.core :as cmr]))

(defn get-orbit-granules!
  [state]
  (let [collections (cmr/search-collections state
                                            {:has_granules true
                                             :page_size 1})
        coll-id (:id (first collections))
        granules (cmr/search-granules state
                                      coll-id
                                      {:page_size 10})]
    granules))
