(ns eval.cmr.orbit
  (:require
   [cheshire.core :as json]
   [maths.cmr.core :as cmr]))


(defn get-orbit-granules!
  [state]
  (let [collections (cmr/get-collections! state
                                          {:has_granules true
                                           :page_size 1})
        coll-id (:id (first collections))
        granules (cmr/get-granules! state
                                    coll-id
                                    {:page_size 10})]
    granules))

#_(get-orbit-granules! {:connections {::cmr/cmr (cmr/cmr :uat)}})

