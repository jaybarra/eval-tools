(ns eval.time-tracker.interface
  (:require
   [eval.time-tracker.core :as core]))

(defn start
  [log event]
  (core/start log event))

(defn stop
  [log event]
  (core/stop log event))
