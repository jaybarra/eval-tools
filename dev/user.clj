(ns user
  "Developer namespace for controlling the app."
  (:require
   [clojure.pprint :refer [pprint pp]]
   [clojure.repl :refer [doc source]]
   [clojure.stacktrace :as st]
   [clojure.tools.namespace.repl :refer [refresh]]
   [integrant.repl :as ig-repl]
   [eval.system :refer [config]]))

(ig-repl/set-prep! config)

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
