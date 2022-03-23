(ns eval.playback.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]))

(def log-file-name "cmr-playback.log")

(timbre/set-level! :info)
(timbre/merge-config! {:appenders
                       {:println {:enabled? false}
                        :spit (appenders/spit-appender {:fname log-file-name})}})
