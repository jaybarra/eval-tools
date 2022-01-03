(ns eval.cmr.commands.jobs
  "Administrative jobs to execute on a Common Metadata Repository.
  These jobs will can be used to trigger events in CMR that are normally
  run on a schedule."
  (:require
   [eval.cmr.client :as cmr]))

(defn cleanup-expired-collections
  "Schedule a job to cleanup expired collections."
  []
  {::cmr/request
   {:method :post
    :url "/ingest/jobs/cleanup-expired-collections"}
   ::cmr/category :admin})

(defn trigger-full-collection-granule-aggregate-cache-refresh
  "Schedule a job to refresh granule aggregate cache."
  []
  {::cmr/request
   {:method :post
    :url "/ingest/jobs/trigger-full-collection-granule-aggregate-cache-refresh"}
   ::cmr/category :admin})

(defn trigger-partial-collection-granule-aggregate-cache-refresh
  []
  {::cmr/request
   {:method :post
    :url "/ingest/jobs/trigger-partial-collection-granule-aggregate-cache-refresh"}
   ::cmr/category :admin})

(defn trigger-granule-task-cleanup
  []
  {::cmr/request
   {:method :post
    :url "/ingest/jobs/trigger-granule-task-cleanup-job"}
   ::cmr/category :admin})
