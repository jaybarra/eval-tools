(ns eval.cmr.bulk.granule
  "CMR Bulk Granule API interaction tools.

  For CMR interaction, the environment variables should be configured
  to include Echo Tokens as defined in the [[eval.cmr.core]] namespace.

  * Create job definitions
  * Submit jobs for processing
  * Check the status of jobs"
  (:require
   [clojure.core.async :as async :refer [go >! <! >!! <!! chan close!]]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [taoensso.timbre :as log])
  (:import
   (java.time Instant)))

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
  [cmr-conn query & [opts]]
  (let [available (cmr/cmr-hits cmr-conn :granule query)
        limit (min available (get opts :limit available))

        scroll-page (partial cmr/scroll! cmr-conn :granule query)

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
        (cmr/clear-scroll-session! cmr-conn scroll-id)))))

(defn scroll-granule-urs->file!
  "Return a filename containing the list of granule URs from CMR based on a query.
  And optional amount value may be specified.

  This is suitable for granule amounts that cannot fit in memory.

  TODO: this is blocking and should have an async version
  See also: [[scroll-granule-urs]]"
  [cmr-conn out-file query xf & [opts]]
  (let [available (cmr/cmr-hits cmr-conn :granule query)
        limit (min available (get opts :limit available))

        scroll-page (partial cmr/scroll! cmr-conn :granule query)

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
            (recur (+ scrolled (count instructions))))))
      (finally
        (cmr/clear-scroll-session! cmr-conn scroll-id)))))

(defn bulk-update-file!
  "Write a bulk granule update job as a json file using a file containing
  a newline-separated list of granules and a transform function for"
  [job-def granule-file out-file]

  #_(when-not (spec/valid? ::bulk-granule-job job-def)
      (throw (ex-info "Invalid bulkd-granule-job definition"
                      (spec/explain-data ::bulk-granule-job job-def))))

  ;; write incomplete update json
  ;; TODO this is not precise enough, verify we are only removing "]}"
  (let [file-data (seq (slurp (muuntaja/encode cmr/m "application/json" job-def)))]
    (spit out-file (str (string/join (drop-last (drop-last file-data))) "\n")))

  (let [ch (async/chan)]
    (go
      (with-open [rdr (io/reader granule-file)]
        (doseq [line (line-seq rdr)]
          (>! ch line)))
      (async/close! ch))
    (loop []
      (when-let [line (<!! ch)]
        ;; TODO this is ugly and not easily understood, rewrite to be cleaner
        (spit out-file (str "[" (string/join "," (map #(str "\"" % "\"") (string/split line #","))) "],\n") :append true)
        (recur))))
  ;; TODO remove trailing comma from final instruction when present
  ;; cap the instructions list
  (spit out-file "\n]}" :append true))

(defn submit-job!
  "POST a bulk granule update job to CMR and return the response."
  [cmr-conn provider job-def]
  (let [url (format "/ingest/providers/%s/bulk-update/granules" provider)
        job (cmr/decode-cmr-response
             (cmr/api-action!
              cmr-conn
              {:method :post
               :url url
               :headers {:content-type "application/json"}
               :body (cmr/encode->json job-def)}))]
    (log/info (format "Bulk Granule Update Job created with ID [%s]" (:task-id job)))
    job))

(defn fetch-job-status
  "Request bulk granule update job status from CMR."
  [cmr-conn job-id]
  (cmr/decode-cmr-response
   (cmr/api-action!
    cmr-conn
    {:method :get
     :url (format "/ingest/granule-bulk-update/status/%s" job-id)})))

(defn benchmark-processing
  "Request status with a delay to compute per-second updates happening
  in the job.
  TODO: make an async, non-blocking version"
  ([cmr-conn task-id]
   (benchmark-processing cmr-conn task-id 3))
  ([cmr-conn task-id time-in-sec]
   (let [get-counts #(->> (fetch-job-status cmr-conn task-id)
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
  [cmr-conn]
  (cmr/decode-cmr-response
   (cmr/api-action! cmr-conn
                    {:method :post
                     :url "/ingest/granule-bulk-update/status"})))

(defn log-benchmark
  "Write a formatted benchmark to the log."
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
