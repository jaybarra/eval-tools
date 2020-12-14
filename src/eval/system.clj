;;; system.clj --- system configuration
;;; Commentary:
;;; Code:
(ns eval.system
  (:require
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [eval.handler :as app]
   [ring.adapter.jetty :as jetty]
   [taoensso.timbre :as log]))

(def system-config (-> "config.edn" io/resource slurp ig/read-string))

(defmethod ig/init-key :adapter/jetty
  [_ {:keys [handler port] :as opts}]
  (log/info "Starting server on port " port)
  (jetty/run-jetty handler (dissoc opts :handler)))

(defmethod ig/halt-key! :adapter/jetty
  [_ server]
  (.stop server))

(defmethod ig/init-key :handler/app
  [_ {message :message}]
  (log/info (or message "No message provided. Good Luck"))
  (app/create-app))

(defn -main
  []
  (ig/init system-config))
;;; system.clj ends here
