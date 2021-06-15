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
   [clojure.spec.alpha :as spec]
   [clojure.string :as str]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [taoensso.timbre :as log]))

(spec/def ::instruction (spec/cat :granule-ur string? :value string?))
(spec/def ::updates (spec/+ ::instruction))
(spec/def ::operation #{"UPDATE_FIELD"
                        "APPEND_TO_FIELD"})
(spec/def ::update-field #{"OPeNDAPLink"
                           "S3Link"})
(spec/def ::name string?)
(spec/def ::provider string?)
(spec/def ::job (spec/keys :req [::operation
                                 ::update-field
                                 ::updates]
                           :opt [::name]))

(defn add-update-instructions
  "Add a list of update instructions to a bulk granule update request.
  based on a transformation."
  [job urs xf]
  (->> urs
       (map (fn [id] [id (xf id)]))
       (assoc job :updates)))

(defn submit-job!
  "POST a bulk granule update job to CMR and return the response."
  [client provider job-def]
  (let [url (format "/ingest/providers/%s/bulk-update/granules" provider)
        job (cmr/decode-cmr-response-body
             (cmr/invoke client
                         {:method :post
                          :url url
                          :headers {:content-type "application/json"}
                          :body (cmr/encode->json job-def)}))]
    (log/info (format "Bulk Granule Update Job created with ID [%s]" (:task-id job)))
    job))

(defn trigger-status-update!
  "Trigger an update of bulk granule job statuses."
  [client]
  (cmr/decode-cmr-response-body
   (cmr/invoke client
               {:method :post
                :url "/ingest/granule-bulk-update/status"})))

(defn fetch-job-status
  "Request bulk granule update job status from CMR."
  [client job-id & [opts]]
  (let [{:keys [show_granules
                show_progress
                show_request]} opts
        query-params {:show_granules (or show_granules false)
                      :show_progress (or show_progress false)
                      :show_request (or show_request false)}]
    (cmr/decode-cmr-response-body
     (cmr/invoke
      client
      {:method :get
       :url (format "/ingest/granule-bulk-update/status/%s" job-id)
       :query-params query-params}))))
