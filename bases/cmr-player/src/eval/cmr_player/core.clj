(ns eval.cmr-player.core
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [eval.cmr-player.runner :as runner]
   [eval.cmr.interface.client :as client]
   ;; extra
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders])
  (:import
   [java.io File PushbackReader]))

;; TODO move this to a component
(timbre/merge-config!
 {:appenders {:println {:enabled? false}
              :spit (appenders/spit-appender {:fname "playback.log"})}})

(def cli-options
  [["-u" "--cmr-url URL" "CMR Instance URL"]
   ["-t" "--token TOKEN" "CMR Authentication Token"]])

(defn load-script
  [f]
  (try
    (with-open [r (io/reader f)]
      (edn/read (PushbackReader. r)))
    (catch java.io.IOException e
      (timbre/error (format "Couldn't open script '%s': %s\n" f (.getMessage e))))
    (catch RuntimeException e
      (timbre/error (format "Error parsing script file '%s': %s\n" f (.getMessage e))))))

(defn -main
  [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-options)
        {:keys [token cmr-url]} options
        script (load-script (first arguments))
        script-relative-root (.getParent (File. (first arguments)))
        client (client/create-client (merge {:url cmr-url}
                                            (when token
                                              {:token token})))
        state {:client client
               :script-relative-root script-relative-root}]
    (printf "Playing script %s%n" (first arguments))
    (timbre/info "Beginning script playback:" (first arguments))
    (time (runner/play-script state script))))
