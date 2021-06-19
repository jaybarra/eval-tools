(ns user
  "Developer namespace for controlling the app."
  (:require
   [clojure.pprint :refer [pprint pp]]
   [clojure.repl :refer [doc source]]
   [clojure.stacktrace :as st]
   [integrant.repl :as ig-repl]
   [eval.system :refer [config]]
   [taoensso.timbre :as log]))

(ig-repl/set-prep! config)

(defn set-logging-level!
  "Set the log level for the system. "
  [level]
  (log/set-level! level))

(set-logging-level! (get-in (config) [:log/system :level] :debug))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(defn context [] integrant.repl.state/system)

(comment
  (go)
  (halt)
  (reset)
  (reset-all)
  (context))
