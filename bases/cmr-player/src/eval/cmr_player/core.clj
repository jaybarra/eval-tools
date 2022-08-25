(ns eval.cmr-player.core
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.tools.logging :as log]
   [eval.cmr-player.runner :as runner]
   [eval.cmr.interface.client :as client])
  (:import
   [java.io File PushbackReader]))

(def cli-options
  [["-u" "--cmr-url URL" "CMR Instance URL"]
   ["-t" "--token TOKEN" "CMR Authentication Token"]])

(defn load-script
  [f]
  (try
    (with-open [r (io/reader f)]
      (edn/read (PushbackReader. r)))
    (catch java.io.IOException e
      (log/error (format "Couldn't open script '%s': %s\n" f (.getMessage e))))
    (catch RuntimeException e
      (log/error (format "Error parsing script file '%s': %s\n" f (.getMessage e))))))

(defn -main
  [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-options)
        {:keys [token cmr-url client-id]} options
        script (load-script (first arguments))
        script-relative-root (.getParent (File. (first arguments)))
        client (client/create-client (merge {:url cmr-url :client-id "CMR-Player"}
                                            (when client-id
                                              {:client-id client-id})
                                            (when token
                                              {:token token})))
        state {:client client
               :script-relative-root script-relative-root}]
    (printf "Playing script %s%n" (first arguments))
    (log/info "Beginning script playback:" (first arguments))
    (log/debug "Final State" (runner/play-script state script))))
