(ns eval.cmr.commands.jobs
  "Administrative jobs to execute on a Common Metadata Repository.
  These jobs will can be used to trigger events in CMR that are normally
  run on a schedule.")

(defn cleanup-expired-collections
  "Schedule a job to cleanup expired collections."
  []
  {:request
   {:method :post
    :url "/ingest/jobs/cleanup-expired-collections"}})

(defn trigger-full-collection-granule-aggregate-cache-refresh
  "Schedule a job to refresh granule aggregate cache."
  []
  {:request
   {:method :post
    :url "/ingest/jobs/trigger-full-collection-granule-aggregate-cache-refresh"}})

(defn trigger-partial-collection-granule-aggregate-cache-refresh
  []
  {:request
   {:method :post
    :url "/ingest/jobs/trigger-partial-collection-granule-aggregate-cache-refresh"}})

(defn trigger-granule-task-cleanup
  []
  {:request
   {:method :post
    :url "/ingest/jobs/trigger-granule-task-cleanup-job"}})
