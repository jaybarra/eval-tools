(ns user
  (:require
   [integrant.repl :as ig-repl]
   [eval.system :as system]))

(ig-repl/set-prep! (constantly system/system-config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(comment
  (go)
  (halt)
  (reset)
  (reset-all))
