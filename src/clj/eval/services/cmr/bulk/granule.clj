(ns eval.services.cmr.bulk.granule
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [eval.cmr.bulk.granule :as bulk-granule]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [taoensso.timbre :as log])
  (:import
   (java.util Scanner)
   (java.time Instant)))

(defn submit-job!
  "POST a bulk granule update job to CMR and return the response."
  [client provider job-def & [opts]]
  (let [job (cmr/decode-cmr-response-body
             (cmr/invoke client (bulk-granule/post-job provider job-def)))]
    (log/info (format "Bulk Granule Update Job created with ID [%s]" (:task-id job)))
    job))

(defn trigger-status-update!
  "Trigger an update of bulk granule job statuses."
  [client & [opts]]
  (cmr/decode-cmr-response-body
   (cmr/invoke client (bulk-granule/trigger-update opts))))

(defn fetch-job-status
  "Request bulk granule update job status from CMR."
  [client job-id & [opts]]

  (let [{:keys [show-granules
                show-progress
                show-request]} opts
        query-params {:show_granules (or show-granules false)
                      :show_progress (or show-progress false)
                      :show_request (or show-request false)}]
    (cmr/decode-cmr-response-body
     (cmr/invoke client (bulk-granule/get-job-status job-id query-params)))))

(defn benchmark
  "Request status with a delay to compute per-second updates happening
  in the bulk granule update job.
  TODO: make an async, non-blocking version"
  ([client task-id]
   (benchmark client task-id 1))
  ([client task-id time-in-sec]
   (let [resp (cmr/decode-cmr-response-body
               (cmr/invoke client
                           (bulk-granule/get-job-status
                            task-id
                            {:show_granules true})))
         get-counts #(->> resp
                          :granule-statuses
                          (map :status)
                          frequencies)
         start-counts (get-counts)
         ;; Note where start and end are calculated from
         ;; The query takes a while so we clock it from when it was
         ;; sent, not when the response comes back
         start-time (Instant/now)

         _ (Thread/sleep (* 1000 time-in-sec))

         end-time (Instant/now)
         end-counts (get-counts)
         duration (quot (- (.toEpochMilli end-time)
                           (.toEpochMilli start-time))
                        1000)]
     {:task-id task-id
      :start-time start-time
      :end-time end-time
      :benchmark-duration duration
      :start-counts start-counts
      :end-counts end-counts
      :processed (- (get start-counts "PENDING" 0)
                    (get end-counts "PENDING" 0))})))

(defn add-update-instructions
  "Add a list of update instructions to a bulk granule update request.
  based on a transformation."
  [job urs xf]
  (->> urs
       (map (fn [id] [id (xf id)]))
       (assoc job :updates)))
