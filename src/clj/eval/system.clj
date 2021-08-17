(ns eval.system
  "Main entrypoint to the Eval Tools system."
  (:require
   [aero.core :as aero :refer [read-config]]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [eval.cmr.core :as cmr]
   [eval.db.document-store :as doc-store]
   [eval.handler :as app]
   [integrant.core :as ig]
   [ring.adapter.jetty :as jetty]
   [taoensso.timbre :as log])
  (:import
   [org.eclipse.jetty.server Server])
  (:gen-class))

(set! *warn-on-reflection* true)

;; Let Aero know how to read integrant references
(defmethod aero/reader 'ig/ref
  [{:keys [profile] :as opts} _tag value]
  (ig/ref value))

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
  [_ {:keys [instances :as opts]}]
  (log/info "CMR application initialized")
  (let [clients (apply merge (for [[k v] instances]
                               {k (cmr/create-client (merge {:id k} v))}))]
    {:instances clients}))

(defmethod ig/init-key :handler/webapp
  [_ {:keys [cmr]}]
  (when-let [banner (io/resource "banner.txt")]
    (log/info (slurp banner)))
  (app/create-app cmr))

(defmethod ig/init-key :adapter/jetty
  [_ {:keys [handler port] :as opts}]
  (log/info "Starting server on port " port)
  (jetty/run-jetty handler (dissoc opts :handler)))

(defmethod ig/halt-key! :adapter/jetty
  [_ ^Server server]
  (.stop server))

(defn -main
  "Main entrypoint when running from uberjar"
  [& args]
  (when (seq args)
    (doseq [arg args]
      (println arg)))
  (ig/init (config)))