(ns eval.db.document-store
  (:require
   [clojure.java.io :as io]
   [crux.api :as crux]
   [taoensso.timbre :as log]))

(defprotocol DocumentStore
  (save! [this document])
  (query [this query])
  (halt! [this]))

(defrecord CruxStore [crux-node]

  DocumentStore

  (save! [this document]
    (crux/submit-tx (:crux-node this) [[:crux.tx/put document]]))

  (query [this query]
    (crux/q (crux/db (:crux-node this)) query))

  (halt! [this]
    (when-let [node (:crux-node this)]
      (.close node)
      (dissoc this :crux-node))))

(defmulti create-document-store :type)

(defmethod create-document-store :default
  [opts]
  ;; TODO convert to in-memory store
  (log/warn "NOOP document store, no persistence")
  (reify DocumentStore
    (save! [this document] (log/debug "NOOP save document" document))
    (query [this query] (log/debug "NOOP query" query))
    (halt! [this] nil)))

(defmethod create-document-store :crux
  [opts]
  (letfn [(kv-store [dir]
            {:kv-store {:crux/module 'crux.rocksdb/->kv-store
                        :db-dir (io/file dir)
                        :sync? true}})]
    (->CruxStore (crux/start-node
                  {:crux/tx-log (kv-store (get opts :log-dir "data/dev/tx-log"))
                   :crux/document-store (kv-store (get opts :doc-dir "data/dev/doc-store"))
                   :crux/index-store (kv-store (get opts :index-dir "data/dev/index-store"))}))))

(defn stop-document-store
  [store]
  (halt! store))
