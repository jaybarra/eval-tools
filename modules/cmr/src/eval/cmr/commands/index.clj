(ns eval.cmr.commands.index
  "Commands relating to indexing concepts within CMR.")

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

(defn reindex-provider-collections
  []
  {:request
   {:method :post
    :url "/reindex-provider-collections"}})
