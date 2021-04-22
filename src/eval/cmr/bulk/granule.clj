(ns eval.cmr.bulk.granule
  (:require
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [taoensso.timbre :as log]))

(def m (muuntaja/create))

(def base-request {:name "large update request"
                   :operation "UPDATE_FIELD"
                   :update-field "OPeNDAPLink"
                   :updates []})

(defn edn->json
  "Convert edn to json string."
  [edn]
  (slurp (muuntaja/encode m "application/json" edn)))

(defn scroll-granules
  "Send a scroll search request to CMR for a list of granules.
  TODO: move this to the [[eval.cmr.core]] namespace, this has nothing
  to do with bulk granule specifically"
  [state request limit]
  (let [{:keys [scroll-id results]} (cmr/scroll-granules
                                     state
                                     request
                                     {:continue true})
        result-cap (min limit (:hits results))]

    (log/info (format "Began scrolling session [%s]" scroll-id))

    (loop [scrolled (:items results)]
      (if (>= (count scrolled) result-cap)
        (do
          (cmr/clear-scroll-session! state scroll-id)
          (log/info (format (str "Completed scrolling for [%s]. "
                                 "Found [%d], Requested [%d], Available [%d]")
                            (pr-str request)
                            (count scrolled)
                            limit
                            (:hits results)))
          scrolled)
        (recur (concat
                scrolled
                (get-in
                 (cmr/scroll-granules
                  state
                  request
                  {:continue true
                   :scroll-id scroll-id})
                 [:results :items])))))))

(defn granule-bulk-update-request
  "Generates a bulk update request json file.
  example: (generate-request-file state base-req query 10000)"
  [state request query size]
  (->> (scroll-granules state query size)
       (map :umm)
       (map :GranuleUR)
       (map (fn [id] [id  (str "http://www.example.com/granule/" id)]))
       (assoc request :updates)))

(defn request->file
  [request filename]
  (->> request
       edn->json
       (spit filename)))

(comment
  (def concept-ids
    (->> (cmr/get-collections (cmr/cmr-state :prod) {:provider "PODAAC"
                                                     :page_size 50})
         :items
         (map :meta)
         (map :concept-id)
         vec))

  (request->file
   (granule-bulk-update-request
    (cmr/cmr-state :prod)
    base-request
    (cmr/->Query {:collection_concept_id concept-ids
                  :page_size 1000})
    20000)
   "bgu_20k.json"))
