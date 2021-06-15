(ns user
  "Developer namespace for controlling the app."
  (:require
   [clojure.pprint :refer [pprint pp]]
   [clojure.repl :refer [doc source]]
   [clojure.stacktrace :as st]
   [integrant.repl :as ig-repl]
   [eval.system :refer [config]]))

(ig-repl/set-prep! config)

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
