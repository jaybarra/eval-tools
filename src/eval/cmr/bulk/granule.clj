(ns eval.cmr.bulk.granule
  "CMR Bulk Granule API interaction tools.

  For CMR interaction, the environment variables should be configured
  to include Echo Tokens as defined in the [[eval.cmr.core]] namespace.

  * Create job definitions
  * Submit jobs for processing
  * Check the status of jobs"
  (:require
   [clj-http.client :as client]
   [clojure.core.async :as async]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [eval.cmr.core :as cmr]
   [eval.utils.conversions :as util]
   [java-time :as jtime]
   [muuntaja.core :as muuntaja]
   [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::instruction (spec/cat :granule-ur string? :url string?))
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
  TODO: this is blocking and should be have an async version"
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

(defn scroll-granule-urs->file
  "Return a filename containing the list of granule URs from CMR based on a query.
  And optional amount value may be specified.

  This is suitable for granule amounts tha cannot fit in memory.

  TODO: this is blocking and should be have an async version
  See also: [[scroll-granule-urs]]"
  [cmr-conn out-file query & [opts]]
  (let [available (cmr/cmr-hits cmr-conn :granule query)
        limit (min available (get opts :limit available))

        scroll-page (partial cmr/scroll! cmr-conn :granule query)

        first-page (scroll-page {:format :umm_json})
        scroll-id (:CMR-Scroll-Id first-page)
        granules (cmr/umm-json-response->items (:response first-page))
        urs (map (comp :GranuleUR :umm) granules)]

    (spit out-file (string/join "\n" urs))

    (try
      (loop [scrolled (count urs)]
        (if (>= scrolled limit)
          (.exists (clojure.java.io/file out-file)
          (let [urs (->> (scroll-page {:format :umm_json
                                       :CMR-Scroll-Id scroll-id})
                         :response
                         cmr/umm-json-response->items
                         (map (comp :GranuleUR :umm)))]
            (spit out-file (string/join "\n" urs) :append true)
            (recur (+ scrolled (count urs))))))
      (finally
        (cmr/clear-scroll-session! cmr-conn scroll-id)))))

(defn submit-job!
  "POST a bulk granule update job to CMR and return the response."
  [cmr-conn provider job-def]
  (let [url (format "/ingest/providers/%s/bulk-update/granules" provider)
        response (cmr/api-action!
                  cmr-conn
                  {:method :post
                   :url url
                   :headers {:content-type "application/json"}
                   :body (cmr/encode->json job-def)})
        job (muuntaja/decode-response-body cmr/m response)]
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
         start-time (System/currentTimeMillis)

         _ (Thread/sleep (* 1000 time-in-sec))

         end-time (System/currentTimeMillis)
         end-counts (get-counts)
         duration (/ (- end-time start-time) 1000.0)]
     {:task-id task-id
      :start-time (jtime/instant start-time)
      :end-time (jtime/instant end-time)
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
        processed-start (get start-counts "PENDING" 0)
        processed-end (get end-counts "PENDING" 0)]
    (log/info
     (format "BENCHMARK: [%d] granules per second over [%d] seconds in task [%s]"
             (/ (- processed-start processed-end) benchmark-duration)
             benchmark-duration
             task-id) benchmark)))

(comment
  (def base-request {:name "large update request"
                     :operation "UPDATE_FIELD"
                     :update-field "OPeNDAPLink"
                     :updates []})

  (def collection-ids
    (->> (cmr/search
          (cmr/cmr-conn :wl)
          :collection
          {:provider "CDDIS"}
          {:format :umm_json})
         cmr/umm-json-response->items
         (map (comp :concept-id :meta))))

  (def granule-urs
    (fetch-granule-urs
     (cmr/cmr-conn :wl)
     {:collection_concept_id collection-ids
      :page_size 2000}
     {:limit 10000}))

  (def job-def
    (add-update-instructions
     base-request
     granule-urs
     (fn [ur] (str "https://example.com/updated/" ur))))

  (def job
    (submit-job!
     (cmr/cmr-conn :wl)
     "CDDIS"
     job-def))

  #_(def benchmarks
      (loop [data []]
        (Thread/sleep 1000)
        (let [benchmark (benchmark-processing (cmr/cmr-conn :sit) 121)]
          (log/info benchmark)
          (if (= (:start-cnt benchmark) (:end-cnt benchmark))
            data
            (recur (conj data benchmark))))))

  ;; verify the change is reflected in searches
  (log/info
   (cmr/decode-cmr-response
    (cmr/search
     (cmr/cmr-conn :prod)
     :granule
     {:granule_ur (first granule-urs)
      :collection_concept_id collection-ids
      :page_size 1}
     {:format :umm_json}))))
