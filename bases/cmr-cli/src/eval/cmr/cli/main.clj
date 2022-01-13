(ns eval.cmr.cli.main
  "Main entrypoint to the Eval Tools system."
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [eval.cmr.interface.client :as cmr]
   [eval.cmr.interface.search :as search]
   [integrant.core :as ig])
  (:gen-class))

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

(defn exec-command
  [context args]
  (println args)
  (let [command (first args)
        client-id (keyword (second args))
        client (get-in context [:instances client-id])
        args (drop 2 args)]
    (when-not client
      (throw (ex-info "Invalid command"
                      {:error "No such client [" client-id "]"
                       :args args})))
    (case command
      "search" (cmr/invoke client (search/search (rest args)))

      ;; default
      (println "unrecognized command [" command "]"))))

(defn -main
  "Main entrypoint when running from uberjar"
  [& args]
  (let [app (:app/cmr (ig/init (config)))]
    (exec-command app args)))

(comment
  (-main "search" "sit" "c" "" "fmt:json"))
