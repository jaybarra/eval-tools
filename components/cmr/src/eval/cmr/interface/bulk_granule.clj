(ns eval.cmr.interface.bulk-granule
  "CMR Bulk Granule API interaction tools.

  For CMR interaction, the environment variables should be configured
  to include Echo Tokens as defined in the [[eval.cmr.client]] namespace.

  * Create job definitions
  * Submit jobs for processing
  * Check the status of jobs"
  (:require
   [eval.cmr.commands.bulk-granule :as bulk-gran]))

(defn post-job
  "Returns a command what will post a bulk-granule-update job to CMR."
  [provider job]
  (bulk-gran/post-job provider job))

(defn trigger-update
  "Returns a command that will trigger an update to the status of bulk-granule-job statuses"
  []
  (bulk-gran/trigger-update))

(defn get-job-status
  "Returns a command that will return the status of a given job."
  [job-id options]
  (bulk-gran/get-job-status job-id options))
