(ns eval.services.cmr.bulk.granule-service
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

(defn bulk-update-file!
  "Write a bulk granule update job as a json file."
  [job-def instruction-file out-file]
  ;; write incomplete update json structure by removing trailing `]}`
  ;; TODO this can break if job keywords are out of order, :updates must be last
  (let [file-data (seq (slurp (muuntaja/encode cmr/m "application/json" job-def)))]
    (spit out-file (str (str/join (drop-last (drop-last file-data))) "\n")))

  ;; write the update instructions
  (with-open [xin (io/input-stream instruction-file)]
    (let [scan (Scanner. xin)]
      (loop [line (.nextLine scan)]
        ;; this is ugly, it's just csv, no need to be this dirty
        (let [line-str (as-> line line-str
                         (str/split line-str #",")
                         (map #(str "\"" % "\"") line-str)
                         (str/join "," line-str)
                         (str "[" line-str "]"))]
          (if (.hasNext scan)
            (do
              (spit out-file (str line-str ",\n") :append true)
              (recur (.nextLine scan)))
            (spit out-file line-str :append true))))))
  ;; cap the file with the missing brackets
  (spit out-file "\n]}" :append true))

(defn benchmark
  "Request status with a delay to compute per-second updates happening
  in the bulk granule update job.
  TODO: make an async, non-blocking version"
  ([client task-id]
   (benchmark client task-id 3))
  ([client task-id time-in-sec]
   (let [get-counts #(->> (bulk-granule/fetch-job-status client task-id)
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

(defn log-benchmark
  "Write a formatted benchmark to the log.

  See [[benchmark]]"
  [benchmark]
  (let [{:keys [start-counts end-counts benchmark-duration task-id]} benchmark
        pending-start (get start-counts "PENDING" 0)
        pending-end (get end-counts "PENDING" 0)]
    (log/info
     (format "BENCHMARK: [%d] granules per second over [%d] seconds in task [%s]"
             (quot (- pending-start pending-end) benchmark-duration)
             benchmark-duration
             task-id)
     benchmark)))
