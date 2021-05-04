(ns eval.cmr.bulk.granule
  (:require
   [clj-http.client :as client]
   [clojure.core.async :as async]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [taoensso.timbre :as log]))

(spec/def ::instruction (spec/cat :granule-ur string? :url string?))
(spec/def ::updates (spec/* ::instruction))
(spec/def ::operation #{"UPDATE_FIELD"})
(spec/def ::update-field #{"OPeNDAPLink" "S3Link"})
(spec/def ::name string?)
(spec/def ::provider string?)

(spec/def ::definition (spec/keys :req-un [::operation
                                           ::update-field
                                           ::updates]
                                  :opt-un [::name]))

(spec/def ::bulk-granule-job (spec/keys :req-un [::definition
                                                 ::provider]
                                        :opt-un [::granules-query]))


(def base-request {:name "large update request"
                   :operation "UPDATE_FIELD"
                   :update-field "OPeNDAPLink"
                   :updates []})

(spec/explain-str ::definition base-request)

(def bulk-granule-job
  {:provider nil
   :definition base-request
   :granules-query {}
   :xf (fn [ur] ur)})

(defn edn->json
  "Convert edn to json string."
  [edn]
  (slurp (muuntaja/encode (cmr/m) "application/json" edn)))

(defn edn->file
  "Write an edn map to a file."
  [edn filename]
  (->> edn
       edn->json
       (spit filename)))

(defn add-update-instructions
  "Add a list of update instructions to a bulk granule update request."
  [request urs xf]
  (->> urs
       (map (fn [id] [id (xf id)]))
       (assoc request :updates)))

(defn fetch-granule-urs
  "Return the list of granule URs from CMR based on a query.
  And optional amount value may be specified."
  [state query & [amount]]
  (->> (cmr/scroll-granules state query (if amount
                                          {:limit amount}
                                          {}))
       (map :umm)
       (map :GranuleUR)))

(defn submit-job!
  "POST a bulk granule update job to CMR and return the response."
  [state provider job-def]
  (let [{cmr-url ::cmr/url
         cmr-env ::cmr/env} (cmr/state->cmr state)
        bgu-url (format "%s/ingest/providers/%s/bulk-update/granules"
                        cmr-url
                        provider)
        response (client/post
                  bgu-url
                  (update-in (cmr/http-request job-def)
                             [:headers]
                             #(merge % {"Echo-Token" (cmr/echo-token cmr-env)})))
        job (muuntaja/decode-response-body (cmr/m) response)]
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
    (muuntaja/decode-response-body (cmr/m) response)))

(defn benchmark-processing
  "Request status with a delay to compute per-second updates happening
  in the job."
  ([state task-id]
   (benchmark-processing state task-id 3))
  ([state task-id time-in-sec]
   (let [start-cnt (get (->> (fetch-job-status state task-id)
                             :granule-statuses
                             (map :status)
                             frequencies)
                        "UPDATED" 0)
         start-time (System/currentTimeMillis)
         _ (Thread/sleep (* 1000 time-in-sec))
         end-time (System/currentTimeMillis)
         end-cnt (get (->> (fetch-job-status state task-id)
                           :granule-statuses
                           (map :status)
                           frequencies)
                      "UPDATED" 0)
         avg (int (/ (- end-cnt start-cnt) time-in-sec))]
     {:task-id task-id
      :start-time start-time
      :end-time end-time
      :duration time-in-sec
      :start-cnt start-cnt
      :end-cnt end-cnt
      :average avg})))

(defn log-benchmark
  [benchmark]
  (let [{:keys [start-cnt end-cnt average duration task-id]} benchmark]
    (log/info
     (format "%d => %d : Processed [%d] granules per second over [%d] seconds in task [%s]"
             start-cnt
             end-cnt
             average
             duration
             task-id))))

(defn benchmark->csv-file
  [benchmarks file]
  (with-open [writer (io/writer file)]
    (let [col-names (map name (keys (first benchmarks)))
          data [col-names]
          data (concat data (for [bench benchmarks] (vals bench)))]
      (csv/write-csv writer data))))

(comment
  (def collection-ids
    (->> (cmr/search-collections
          (cmr/cmr-state :prod)
          {:provider "CDDIS"
           :page_size 10})
         :items
         (map :meta)
         (map :concept-id)))

  (def granule-urs
    (fetch-granule-urs
     (cmr/cmr-state :prod)
     {:collection_concept_id collection-ids
      :page_size 100}
     100))

  (def job-def
    (add-update-instructions
     base-request
     granule-urs
     (fn [ur] (str "https://example.com/updated/" ur))))

  (def job
    (submit-job!
     (cmr/cmr-state :wl)
     "LAADS"
     job-def))

  (def benchmarks
    (loop [data []]
      (Thread/sleep 1000)
      (let [benchmark (benchmark-processing (cmr/cmr-state :wl) (:task-id job))]
        (log-benchmark benchmark)
        (if (= (:start-cnt benchmark) (:end-cnt benchmark))
          data
          (recur (conj data benchmark))))))

  ;; verify the change is reflected in searches
  (log/info
   (cmr/search-granules
    (cmr/cmr-state :wl)
    {:granule_ur (first granule-urs)
     :collection_concept_id collection-ids})))
