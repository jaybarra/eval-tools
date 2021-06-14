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
  [_opts]
  (letfn [(kv-store [dir]
            {:kv-store {:crux/module 'crux.rocksdb/->kv-store
                        :db-dir (io/file dir)
                        :sync? true}})]
    (->CruxStore (crux/start-node
                  {:crux/tx-log (kv-store "data/dev/tx-log")
                   :crux/document-store (kv-store "data/dev/doc-store")
                   :crux/index-store (kv-store "data/dev/index-store")}))))

(defn stop-document-store
  [store]
  ;; this is crux specific, fix with protocol
  (halt! store))

(comment
  ;; start
  (def cn (create-document-store nil))

  ;; ingest
  (crux/submit-tx cn [[:crux.tx/put
                       {:crux.db/id "hi2u"
                        :user/name "zig"}]])

  ;; query
  (crux/q (crux/db cn) '{:find [e]
                         :where [[e :user/name "zig"]]})

  ;; halt
  (stop-document-store cn))
