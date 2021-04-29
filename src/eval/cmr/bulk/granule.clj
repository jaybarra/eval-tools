(ns eval.cmr.bulk.granule
  (:require
   [clj-http.client :as client]
   [clojure.string :as string]
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

(defn edn->file
  "Write an edn map to a file."
  [edn filename]
  (->> edn
       edn->json
       (spit filename)))

(defn add-update-instructions
  "Add a list of update instructions to a bulk granule update request."
  [urs request updater-fn]
  (->> urs
       (map (fn [id] [id  (updater-fn id)]))
       (assoc request :updates)))

(defn fetch-granule-urs
  "Return a list of granule URs from CMR."
  [state query amount]
  (->> (cmr/scroll-granules state query {:limit amount})
       (map :umm)
       (map :GranuleUR)))

(defn create-bulk-granule-update-job
  "Submit a bulk granule update request to CMR and return the task-id."
  [state provider task-def]
  (let [{cmr-url ::cmr/url
         cmr-env ::cmr/env} (cmr/state->cmr state)
        bgu-url (format "%s/ingest/providers/%s/bulk-update/granules"
                        cmr-url
                        provider)
        response (client/post
                  bgu-url
                  (update-in (cmr/http-request task-def)
                             [:headers]
                             #(merge % {"Echo-Token" (cmr/echo-token cmr-env)})))
        task (muuntaja/decode-response-body m response)]
    (log/info (format "Bulk Granule Update Job created with ID [%s]" (:task-id task)))
    task))

(defn fetch-job-status
  "Request bulk granule update job status from CMR."
  [state job-id]
  (let [{cmr-url ::cmr/url cmr-env ::cmr/env} (cmr/state->cmr state)
        response (client/get (format "%s/ingest/granule-bulk-update/status/%d"
                                     cmr-url
                                     job-id)
                             {:headers {"Echo-Token" (cmr/echo-token cmr-env)}})]
    (muuntaja/decode-response-body m response)))

(defn benchmark-processing
  "Blocking benchmark request for processing."
  ([task-id]
   (benchmark-processing task-id 3))
  ([task-id time-in-sec]
   (let [start-cnt (get (->> (fetch-job-status (cmr/cmr-state :wl) task-id)
                             :granule-statuses
                             (map :status)
                             frequencies)
                        "UPDATED")
         start-time (System/nanoTime)
         _ (Thread/sleep (* 1000 time-in-sec))
         end-time (System/nanoTime)
         end-cnt (get (->> (fetch-job-status (cmr/cmr-state :wl) task-id)
                           :granule-statuses
                           (map :status)
                           frequencies)
                      "UPDATED")
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
     (format "%d => %d : Processed [%d] per second over [%d] seconds in task [%s]"
             start-cnt
             end-cnt
             average
             duration
             task-id))))

(comment
  (def concept-ids
    (->> (cmr/search-collections
          (cmr/cmr-state :prod)
          {:provider "PODAAC"
           :page_size 100})
         :items
         (map :meta)
         (map :concept-id)))

  (def granule-urs
    (fetch-granule-urs
     (cmr/cmr-state :prod)
     {:collection_concept_id concept-ids
      :page_size 2000}
     20000))

  (def job-def (add-update-instructions
                granule-urs
                base-request
                (fn [ur] (str "https://example.com/granule/" ur))))

  (def job (create-bulk-granule-update-job
            (cmr/cmr-state :wl)
            "PODAAC"
            job-def))

  (log-benchmark (benchmark-processing (:task-id job))))
