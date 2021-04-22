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
  "Send a scroll search request to CMR for a list of granules."
  [state request limit]
  (let [{:keys [scroll-id results]} (cmr/scroll-granules
                                     state
                                     request
                                     {:continue true})
        result-cap (min limit (:hits results))]

    (log/info (format "Began scrolling sesession [%s]" scroll-id))

    (loop [scrolled (:items results)]
      (if (>= (count scrolled) result-cap)
        (do
          (cmr/clear-scroll-session! state scroll-id)
          (log/info (format (str"Completed scrolling for [%s]. "
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
  example: (generate-request-file r 10000 bulk-update.json)"
  [state request query size]
  (->> (scroll-granules state query size)
       (map :meta)
       (map :concept-id)
       (map (fn [id] [id  (str "http://www.example.com/granule/" id)]))
       (assoc request :updates)))

(defn request->file
  [request filename]
  (->> request
       edn->json
       (spit filename)))

(comment
  (request->file
   (granule-bulk-update-request
    (cmr/cmr-state :prod)
    base-request
    (cmr/->Query {:collection_concept_id ["C1674794625-PODAAC"
                                          "C1674794634-PODAAC"
                                          "C1652977738-PODAAC"
                                          "C1649553027-PODAAC"
                                          "C1649549053-PODAAC"
                                          "C1649539559-PODAAC"
                                          "C1649549176-PODAAC"
                                          "C1649539971-PODAAC"
                                          "C1649549419-PODAAC"
                                          "C1649540312-PODAAC"]})
    100)
   "bgu.json"))
