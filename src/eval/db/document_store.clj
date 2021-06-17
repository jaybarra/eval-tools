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

(defn create-document-store
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
  ;; this is crux specific, fix with protocol
  (halt! store))
