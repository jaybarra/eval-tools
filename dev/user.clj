(ns user
  "Developer namespace for controlling the app."
  (:require
   [clojure.pprint :refer [pprint pp]]
   [clojure.repl :refer [doc source]]
   [clojure.stacktrace :as st]
   [clojure.tools.namespace.repl :refer [refresh]]
   [integrant.repl :as ig-repl]
   [eval.system :refer [config]]
   [taoensso.timbre :as log]))

(ig-repl/set-prep! config)

(defn set-logging-level!
  "Set the log level for the system"
  [level]
  (log/set-level! level))

(set-logging-level! (get-in (config) [:app/logging :level] :debug))

(defn go
  []
  (refresh)
  (ig-repl/go))

(def halt ig-repl/halt)

(defn reset []
  (refresh)
  (ig-repl/reset))

(defn reset-all
  []
  (refresh)
  (ig-repl/reset-all))

(defn context [] integrant.repl.state/system)

(comment
  (go)
  (halt)
  (reset)
  (reset-all)
  (context))
