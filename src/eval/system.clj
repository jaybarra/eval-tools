(ns eval.system
  "Main entrypoint to the Eval Tools system."
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [eval.cmr.core :as cmr]
   [eval.db.document-store :as doc-store]
   [integrant.core :as ig]
   [taoensso.timbre :as log])
  (:gen-class))

(set! *warn-on-reflection* true)

;; Let Aero know how to read integrant references
(defmethod aero/reader 'ig/ref [_ _ value] (ig/ref value))

;; default handler for integrant
(defmethod ig/init-key :default [_ _cfg])

(defn config
  "Read the config file and return a value or map.
  If no keys are specified, the entire map will be returned, otherwise
  specific keys may be passed in to get specific value. Nil will be returned
  if no matching value exists.

  Config location looks for :eval-config-location"
  [& keys]
  (let [cfg-file (env :eval-config-location "config.edn")
        cfg (aero/read-config (io/resource cfg-file))]
    (get-in cfg keys)))

(defmethod ig/init-key :db/document-store
  [_ opts]
  (let [store (doc-store/create-document-store opts)]
    (log/info "Document Store initialized")
    store))

(defmethod ig/halt-key! :db/document-store
  [_ store]
  (log/info "Shutting down Document Store")
  (doc-store/stop-document-store store))

(defmethod ig/init-key :app/cmr
  [_ {:keys [instances]}]
  (log/info "CMR application initialized")

  (when-let [banner (io/resource "banner.txt")]
    (log/info (slurp banner)))

  (let [clients (apply merge (for [[k v] instances]
                               {k (cmr/create-client (merge {:id k} v))}))]
    {:instances clients}))

(defn -main
  "Main entrypoint when running from uberjar"
  [& args]
  (when (seq args)
    (doseq [arg args]
      (log/info arg)))
  (ig/init (config)))
