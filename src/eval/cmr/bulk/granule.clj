(ns eval.cmr.bulk.granule
  (:require
   [clj-http.client :as client]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [muuntaja.format.json :as json-format]
   [taoensso.timbre :as log]))

(def m (muuntaja/create))

(def base-request {:name "large update request"
                   :operation "UPDATE_FIELD"
                   :update-field "OPeNDAPLink"
                   :updates []})

(defn request->json
  "Convert edn to json"
  [request]
  (muuntaja/encode m "application/json" request))

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

(defn my-test-search
  [size]
  (->> (scroll-granules (cmr/cmr-state :prod)
                        (cmr/->Query {:collection_concept_id "C1674794625-PODAAC"})
                        size)
       (map :meta)
       (map :concept-id)
       (map (fn [id] [id  (str "http://www.example.com/granule/" id)]))
       (assoc base-request :updates)
       request->json
       slurp
       (spit "bgu.json")))

(comment
  (def collections '("C1674794625-PODAAC"
                     "C1674794634-PODAAC"
                     "C1652977738-PODAAC"
                     "C1649553027-PODAAC"
                     "C1649549053-PODAAC"
                     "C1649539559-PODAAC"
                     "C1649549176-PODAAC"
                     "C1649539971-PODAAC"
                     "C1649549419-PODAAC"
                     "C1649540312-PODAAC"))




  (assoc base-request :updates)
  request->json
  slurp
  (spit "bgu.json")



  (->> base-request
       request->json
       slurp
       (spit "big-request.json")))
