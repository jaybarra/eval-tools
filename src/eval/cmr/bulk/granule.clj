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

(defn post-job
  [provider job]
  (let [url (format "/ingest/providers/%s/bulk-update/granules" provider)]
    {:method :post
     :url url
     :headers {"Content-Type" "application/json"}
     :body (cmr/encode->json job)}))

(defn trigger-update
  []
  {:method :post
   :url "/ingest/granule-bulk-update/status"})

(defn get-job-status
  [job-id & [opts]]
  (let [req {:method :get
             :url (format "/ingest/granule-bulk-update/status/%s" job-id)}]
    (if opts
      (assoc req :query-params opts)
      req)))
