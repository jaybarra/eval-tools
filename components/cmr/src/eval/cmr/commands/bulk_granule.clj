(ns eval.cmr.commands.bulk-granule
  "CMR Bulk Granule API interaction tools.

  For CMR interaction, the environment variables should be configured
  to include Echo Tokens as defined in the [[eval.cmr.client]] namespace.

  * Create job definitions
  * Submit jobs for processing
  * Check the status of jobs"
  (:require
   [clojure.spec.alpha :as spec]
   [eval.cmr.client :as cmr]))

(spec/def ::instruction (spec/cat :granule-ur string? :value string?))
(spec/def ::updates (spec/+ ::instruction))
(spec/def ::operation #{"UPDATE_FIELD"
                        "APPEND_TO_FIELD"})
(spec/def ::update-field #{"OPeNDAPLink"
                           "S3Link"
                           "Checksum"})
(spec/def ::name string?)
(spec/def ::provider string?)
(spec/def ::job (spec/keys :req [::operation
                                 ::update-field
                                 ::updates]
                           :opt [::name]))

(defn post-job
  "Returns a command what will post a bulk-granule-update job to CMR."
  [provider job & [opts]]
  (let [url (format "/ingest/providers/%s/bulk-update/granules" provider)
        command {::cmr/request
                 {:method :post
                  :url url
                  :headers {"Content-Type" "application/json"}
                  :body job}
                 ::cmr/category :bulk-update}]

    (if opts
      (assoc command :opts opts)
      command)))

(defn trigger-update
  "Returns a command that will trigger an update to the status of bulk-granule-job statuses"
  [& [opts]]
  (let [command {::cmr/request {:method :post
                                :url "/ingest/granule-bulk-update/status"}
                 ::cmr/category :admin}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn get-job-status
  "Returns a command that will return the status of a given job."
  [job-id & [{progress? :show-progress
              granules? :show-granules
              request? :show-request :as opts}]]
  (let [req (cond-> {:method :get
                     :url (format "/ingest/granule-bulk-update/status/%s" job-id)}
              progress? (assoc-in [:query-params "show_progress"] true)
              granules? (assoc-in [:query-params "show_granules"] true)
              request? (assoc-in [:query-params "show_request"] true))
        command {::cmr/request req
                 ::cmr/category :admin}
        command-opts (dissoc opts :show-progress :show-granules :show-request)]
    (if (seq (keys command-opts))
      (assoc command :opts command-opts)
      command)))
