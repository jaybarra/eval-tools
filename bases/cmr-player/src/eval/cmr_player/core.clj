(ns eval.cmr-player.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [eval.cmr-player.runner :as runner]
   [eval.cmr.interface.client :as client])
  (:import
   java.io.PushbackReader))

(def cli-options
  [["-u" "--cmr-url URL" "CMR Instance URL"]
   ["-t" "--token TOKEN" "CMR Authentication Token"]])

(defn load-script
  [f]
  (try
    (with-open [r (io/reader f)]
      (edn/read (PushbackReader. r)))
    (catch java.io.IOException e
      (printf "Couldn't open script '%s': %s\n" f (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing script file '%s': %s\n" f (.getMessage e)))))

(defn -main
  [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-options)
        {:keys [token cmr-url]} options
        script (load-script (first arguments))
        client (client/create-client (merge {:url cmr-url}
                                            (when token
                                              {:token token})))]
    (runner/play-script client script)))
