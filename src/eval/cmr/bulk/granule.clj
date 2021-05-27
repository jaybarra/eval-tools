(ns eval.cmr.bulk.granule
  "CMR Bulk Granule API interaction tools.

  For CMR interaction, the environment variables should be configured
  to include Echo Tokens as defined in the [[eval.cmr.core]] namespace.

  * Create job definitions
  * Submit jobs for processing
  * Check the status of jobs"
  (:require
   [clojure.core.async :as a]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [taoensso.timbre :as log])
  (:import
   (java.time Instant)
   (java.util Scanner)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::instruction (spec/cat :granule-ur string? :value string?))
(spec/def ::updates (spec/* ::instruction))
(spec/def ::operation #{"UPDATE_FIELD" "APPEND_TO_FIELD"})
(spec/def ::update-field #{"OPeNDAPLink" "S3Link"})
(spec/def ::name string?)
(spec/def ::provider string?)
(spec/def ::bulk-granule-job (spec/keys :req [::operation
                                              ::update-field
                                              ::updates]
                                        :opt [::name]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn add-update-instructions
  "Add a list of update instructions to a bulk granule update request.
  based on a transform."
  [job urs xf]
  (->> urs
       (map (fn [id] [id (xf id)]))
       (assoc job :updates)))

(defn scroll-granule-urs
  "Return the list of granule URs from CMR based on a query.
  And optional amount value may be specified.

  TODO: this is blocking and should have an async version"
  [client query & [{ch :ch :as opts}]]
  (let [available (cmr/query-hits client :granule query)
        limit (min available (get opts :limit available))

        scroll-page (partial cmr/scroll! client :granule query)

        first-page (scroll-page {:format :umm_json})
        scroll-id (:CMR-Scroll-Id first-page )
        granules (cmr/umm-json-response->items (:response first-page))
        urs (map (comp :GranuleUR :umm) granules)]
    (try
      (loop [urs urs]
        (if (>= (count urs) limit)
          urs
          (recur (->> (scroll-page {:format :umm_json
                                    :CMR-Scroll-Id scroll-id})
                      :response
                      cmr/umm-json-response->items
                      (map (comp :GranuleUR :umm))
                      (concat urs)))))
      (finally
        (cmr/clear-scroll-session! client scroll-id)))))

(defn scroll-granule-urs->file!
  "Return a filename containing the list of granule URs from CMR based
  on a query. An optional amount value may be specified.

  This is suitable for granule amounts that cannot fit in memory.

  TODO: this is blocking and should have an async version

  See also: [[scroll-granule-urs]]"
  [client out-file query xf & [opts]]
  (let [available (cmr/query-hits client :granule query)
        limit (min available (get opts :limit available))

        scroll-page (partial cmr/scroll! client :granule query)

        first-page (scroll-page {:format :umm_json})
        scroll-id (:CMR-Scroll-Id first-page)
        granules (cmr/umm-json-response->items (:response first-page))
        instructions (->> granules
                          (map (comp :GranuleUR :umm))
                          (map xf))]

    (spit out-file (string/join "\n" instructions))
    (try
      (loop [scrolled (count instructions)]
        (log/debug (str scrolled " granule urs written to " out-file))
        (if (>= scrolled limit)
          (.exists (io/file out-file))
          (let [instructions (->> (scroll-page {:format :umm_json
                                                :CMR-Scroll-Id scroll-id})
                                  :response
                                  cmr/umm-json-response->items
                                  (map (comp :GranuleUR :umm))
                                  (map xf))]
            (spit out-file (string/join "\n" instructions) :append true)
            (spit out-file "\n" :append true)
            (recur (+ scrolled (count instructions))))))
      (finally
        (cmr/clear-scroll-session! client scroll-id)))))

(defn bulk-update-file!
  "Write a bulk granule update job as a json file using a file containing
  a newline-separated list of granules and a transform function for"
  [job-def instruction-file out-file]

  ;; write incomplete update json structure by removing trailing `]}`
  ;; TODO this can break if job keywords are out of order, :updates must be last
  (let [file-data (seq (slurp (muuntaja/encode cmr/m "application/json" job-def)))]
    (spit out-file (str (string/join (drop-last (drop-last file-data))) "\n")))

  ;; write the instructions
  (with-open [xin (io/input-stream instruction-file)]
    (let [scan (Scanner. xin)]
      (loop [line (.nextLine scan)]
        ;; this is ugly, it's just csv, no need to be this dirty
        (let [line-str (as-> line line-str
                         (string/split line-str #",")
                         (map #(str "\"" % "\"") line-str)
                         (string/join "," line-str)
                         (str "[" line-str "]"))]
          (if (.hasNext scan)
            (do
              (spit out-file (str line-str ",\n") :append true)
              (recur (.nextLine scan)))
            (spit out-file line-str :append true))))))
  ;; cap the file with the missing json closures
  (spit out-file "\n]}" :append true))

(defn submit-job!
  "POST a bulk granule update job to CMR and return the response."
  [client provider job-def]
  (let [url (format "/ingest/providers/%s/bulk-update/granules" provider)
        job (cmr/decode-cmr-response
             (cmr/invoke client
                         {:method :post
                          :url url
                          :headers {:content-type "application/json"}
                          :body (cmr/encode->json job-def)}))]
    (log/info (format "Bulk Granule Update Job created with ID [%s]" (:task-id job)))
    job))

(defn fetch-job-status
  "Request bulk granule update job status from CMR."
  [client job-id]
  (cmr/decode-cmr-response
   (cmr/invoke
    client
    {:method :get
     :url (format "/ingest/granule-bulk-update/status/%s" job-id)})))

(defn benchmark
  "Request status with a delay to compute per-second updates happening
  in the bulk granule update job.
  TODO: make an async, non-blocking version"
  ([client task-id]
   (benchmark client task-id 3))
  ([client task-id time-in-sec]
   (let [get-counts #(->> (fetch-job-status client task-id)
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

(defn trigger-status-update!
  "Trigger an update of bulk granule job statuses."
  [client]
  (cmr/decode-cmr-response
   (cmr/invoke client
               {:method :post
                :url "/ingest/granule-bulk-update/status"})))

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
