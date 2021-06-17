(ns eval.cmr.index
  "Indexer app commands. Indexer does not have an exposed sub-resource
  and must be sent commands directly.")

(defn reindex-provider-collections-command
  []
  {:method :post
   :url "/reindex-provider-collections"})

(defn db-migrate-command
  []
  {:method :post
   :url "/db-migrate"})
