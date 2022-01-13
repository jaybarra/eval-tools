(ns eval.cmr.interface.admin
  (:require
   [eval.cmr.commands.index :as index]
   [eval.cmr.commands.jobs :as jobs]))

(defn reindex-all-collections
  "Schedule a job to reindex all collections.
  This will update all collection indexes in CMR"
  []
  (index/reindex-all-collections))

(defn reindex-collection-permitted-groups
  "Schedule an immediate job to reindex permitted groups on collections. This is
  useful when altering ACLs and search results need to be made available
  quickly."
  []
  (index/reindex-collection-permitted-groups))

(defn reindex-autocomplete-suggestions
  "Schedule a job to update the autocomplete suggestions list. The autocomplete
  suggestions are normally updated automatically but if a new term is desired
  running this will index all newly ingested items and the autocomplete suggestions
  will become available."
  []
  (index/reindex-autocomplete-suggestions))

(defn reindex-provider-collections
  []
  (index/reindex-provider-collections))


(defn cleanup-expired-collections
  "Schedule a job to cleanup expired collections."
  []
  (jobs/cleanup-expired-collections))

(defn trigger-full-collection-granule-aggregate-cache-refresh
  "Schedule a job to refresh granule aggregate cache."
  []
  (jobs/trigger-full-collection-granule-aggregate-cache-refresh))

(defn trigger-partial-collection-granule-aggregate-cache-refresh
  []
  (jobs/trigger-partial-collection-granule-aggregate-cache-refresh))

(defn trigger-granule-task-cleanup
  []
  (jobs/trigger-granule-task-cleanup))
