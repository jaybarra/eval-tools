(ns eval.system
  "Main entrypoint to the Eval Tools system."
  (:require
   [aero.core :as aero :refer [read-config]]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [eval.handler :as app]
   [ring.adapter.jetty :as jetty]
   [taoensso.timbre :as log])
  (:gen-class))

(defn config
  "Read the config"
  []
  (aero/read-config (io/resource "config.edn")))

(defmethod ig/init-key :adapter/jetty
  [_ {:keys [handler port] :as opts}]
  (log/info "Starting server on port " port)
  (jetty/run-jetty handler (dissoc opts :handler)))

(defmethod ig/halt-key! :adapter/jetty
  [_ server]
  (.stop server))

(defmethod ig/init-key :handler/app
  [_ {message :welcome-message}]
  (println (or message "Good Luck!!"))
  (when-let [banner (io/resource "banner.txt")]
    (log/info (slurp banner)))
  (app/create-app))

;; default handler for integrant
(defmethod ig/init-key :default [_ _cfg])

(defn -main
  []
  (ig/init (config)))
