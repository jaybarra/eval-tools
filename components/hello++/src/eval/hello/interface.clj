(ns eval.hello.interface
  (:require
   [eval.hello.core :as core]))

(defn greet
  [name]
  (core/greet name))
