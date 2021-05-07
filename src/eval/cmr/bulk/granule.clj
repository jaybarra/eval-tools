(ns eval.cmr.bulk.granule
  "CMR Bulk Granule API interaction tools.

  For CMR interaction, the environment variables should be configured
  to include Echo Tokens as defined in the [[eval.cmr.core]] namespace.

  * Create job definitions
  * Submit jobs for processing
  * Check the status of jobs"
  (:require
   [clj-http.client :as client]
   [clojure.core.async :as async :refer [chan go >! <!]]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
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
  "Add a list of update instructions to a bulk granule update request."
  [request urs xf]
  (->> urs
       (map (fn [id] [id (xf id)]))
       (assoc request :updates)))

(defn fetch-granule-urs
  "Return the list of granule URs from CMR based on a query.
  And optional amount value may be specified.
  TODO: this is blocking and should be have an async version"
  [state query & [amount]]
  (->> (cmr/scroll-granules state query (if amount
                                          {:limit amount}
                                          {}))
       (map :umm)
       (map :GranuleUR)))

(defn submit-job!
  "POST a bulk granule update job to CMR and return the response."
  [state provider job-def]
  (let [{cmr-url ::cmr/url cmr-env ::cmr/env} (cmr/state->cmr state)
        bgu-url (format "%s/ingest/providers/%s/bulk-update/granules"
                        cmr-url
                        provider)
        response (client/post
                  bgu-url
                  (update-in (cmr/http-request job-def)
                             [:headers]
                             #(merge % {"Echo-Token" (cmr/echo-token cmr-env)})))
        job (muuntaja/decode-response-body cmr/m response)]
    (log/info (format "Bulk Granule Update Job created with ID [%s]" (:task-id job)))
    job))

(defn fetch-job-status
  "Request bulk granule update job status from CMR."
  [state job-id]
  (let [{cmr-url ::cmr/url cmr-env ::cmr/env} (cmr/state->cmr state)
        response (client/get (format "%s/ingest/granule-bulk-update/status/%d"
                                     cmr-url
                                     job-id)
                             {:headers {"Echo-Token" (cmr/echo-token cmr-env)}})]
    (muuntaja/decode-response-body cmr/m response)))

(defn benchmark-processing
  "Request status with a delay to compute per-second updates happening
  in the job.
  TODO: make an async, non-blocking version"
  ([state task-id]
   (benchmark-processing state task-id 3))
  ([state task-id time-in-sec]
   (let [get-counts #(->> (fetch-job-status state task-id)
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
  [state]
  (let [{cmr-url ::cmr/url cmr-env ::cmr/env} (cmr/state->cmr state)]
    (log/info (format "Triggering bulk granule job status updates in [%s]" cmr-env))
    (client/post (format "%s/ingest/granule-bulk-update/status" cmr-url)
                 {:headers {"Echo-Token" (cmr/echo-token cmr-env)}})))

#_(defn async-benchmark-processing
    "Request status with a delay to compute per-second updates happening
  in the job. "
    ([state task-id]
     (benchmark-processing state task-id 3))
    ([state task-id time-in-sec]
     (go
       (let [c (chan)]
         (>! c
             (get (->> (fetch-job-status state task-id)
                       :granule-statuses
                       (map :status)
                       frequencies)
                  "UPDATED" 0))
         (println (<! c))))))

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
    (->> (cmr/search-collections
          (cmr/cmr-state :prod)
          {:provider "CDDIS"
           :page_size 150})
         :items
         (map :meta)
         (map :concept-id)))

  #_(def granule-urs
      (fetch-granule-urs
       (cmr/cmr-state :prod)
       {:collection_concept_id collection-ids
        :page_size 2000}))

  (def granule-urs
    (let [query {:collection_concept_id collection-ids
                 :page_size 2000}
          max-hits (cmr/cmr-hits (cmr/cmr-state :prod) :granule query)
          {:keys [scroll-id results]}
          (cmr/scroll-next!
           (cmr/cmr-state :prod) query)]
      (loop [scrolled 0]
        (if (<= max-hits scrolled)
          (cmr/clear-scroll-session! (cmr/cmr-state :prod) scroll-id)
          (let [granules (:results (cmr/scroll-next! (cmr/cmr-state :prod) query))
                urs (->> granules
                         (map :umm)
                         (map :GranuleUR)
                         (string/join "\n"))]
            ;; side effect + IO, can this be better?
            (spit "1m.txt" urs :append true)
            (recur (+ scrolled (count urs))))))))

  (spit "1m_update.urs.txt" granule-urs)

  (def job-def
    (add-update-instructions
     base-request
     granule-urs
     (fn [ur] (str "https://example.com/updated/" ur))))

  (spit "1m_update.json" (slurp (muuntaja/encode cmr/m "application/json" job-def)))

  (def job
    (submit-job!
     (cmr/cmr-state :wl)
     "CDDIS"
     job-def))

  (def benchmarks
    (loop [data []]
      (Thread/sleep 1000)
      (let [benchmark (benchmark-processing (cmr/cmr-state :sit) 121)]
        (log/info benchmark)
        (if (= (:start-cnt benchmark) (:end-cnt benchmark))
          data
          (recur (conj data benchmark))))))

  ;; verify the change is reflected in searches
  (log/info
   (cmr/search-granules
    (cmr/cmr-state :wl)
    {:granule_ur (first granule-urs)
     :collection_concept_id collection-ids})))
