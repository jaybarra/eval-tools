(ns eval.cmr-cli.core
  "Main entrypoint to the Eval Tools system."
  (:gen-class)
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [eval.cmr.interface.client :as cmr]
   [eval.user-input.interface :as input]
   [integrant.core :as ig]))

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

(defmethod ig/init-key :app/cmr
  [_ {:keys [instances]}]
  (let [clients (apply merge (for [[inst-id cfg] instances]
                               {inst-id (cmr/create-client cfg)}))]
    {:instances clients}))

(defn -main
  "Main entrypoint for the 'cmrc' command."
  [& args]
  (let [app (:app/cmr (ig/init (config)))]
    #_(exec-command app (input/parse-args args))))

(comment
  (-main "search" "sit" "c" "" "fmt:json"))
