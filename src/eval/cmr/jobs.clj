(ns eval.cmr.jobs
  "Administrative jobs to execute on a Common Metadata Repository.
  These jobs will can be used to trigger events in CMR that are normally
  run on a schedule.")

(defn reindex-all-collections
  "Schedule a job to reindex all collections.
  This will update all collection indexes in CMR"
  ([]
   (reindex-all-collections false))
  ([force-version-update?]
   (let [req {:method :post
              :url "/ingest/jobs/reindex-all-collections"}]
     {:request (if force-version-update?
                 (assoc req :query-params {:force_version "true"})
                 req)})))

(defn reindex-collection-permitted-groups
  "Schedule an immediate job to reindex permitted groups on collections. This is
  useful when altering ACLs and search results need to be made available
  quickly."
  []
  {:request
   {:method :post
    :url "/ingest/jobs/reindex-collection-permitted-groups"}})

(defn reindex-autocomplete-suggestions
  "Schedule a job to update the autocomplete suggestions list. The autocomplete
  suggestions are normally updated automatically but if a new term is desired
  running this will index all newly ingested items and the autocomplete suggestions
  will become available."
  []
  {:request
   {:method :post
    :url "/ingest/jobs/reindex-autocomplete-suggestions"}})

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
