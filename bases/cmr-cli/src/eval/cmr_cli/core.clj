(ns eval.cmr-cli.core
  "Main entrypoint to the Eval Tools[CMR Client]"
  (:gen-class)
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [eval.cmr.interface.client :as cmr]
   [eval.cmr.interface.ingest :as cmr-ingest]
   [eval.cmr.interface.search :as cmr-search]
   [eval.user-input.interface :as user-input]
   [fipp.edn :refer [pprint] :rename {pprint fipp}]
   [integrant.core :as ig]
   [jsonista.core :as json]
   [clojure.tools.logging :as log]))

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

(defn print-help
  []
  (println "search | community-usage-metrics"))

(defn -main
  "Main entrypoint for the 'cmr-cli' command.

  |Supported Options|                               |
  |-----------------|-------------------------------|
  | `search`        | sends a search request to CMR |"
  [& args]
  (let [app (:app/cmr (ig/init (config)))
        input (user-input/parse-params args)
        cmr (get-in app [:instances (keyword (:cmr input))])]
    (when-let [command (case (:cmd input)
                         :search (cmr-search/search (:concept-type input)
                                                    (:query input)
                                                    {:format (:format input)})
                         :community-usage-metrics (cmr-search/fetch-community-usage-metrics)
                         :ingest (cmr-ingest/create-concept (:concept-type input)
                                                            (:provider input)
                                                            (:concept input)
                                                            input)

                         (print-help))]
      (try
        (when-let [result (-> (cmr/invoke cmr command)
                              :body
                              (json/read-value json/keyword-keys-object-mapper))]
          (fipp result))
        (catch Exception exception
          (log/error exception
                     "An error occurred while running the command"))))))

(comment
  (-main "search" "cmr:sit" "concept-type:collection" "format:json")
  (-main "search" "cmr:prod" "concept-type:collection" "format:json")
  (-main "community-usage-metrics" "cmr:prod")
  (-main "ingest"
         "cmr:sit"
         "concept-type:c"
         "prov:prov1"
         "nid:sample-native-id"
         "./file/path/collection.iso"
         ":no-exit"))
