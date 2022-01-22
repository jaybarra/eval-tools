(ns eval.user-input.interface
  (:require
   [eval.user-input.core :as core]))

(defn parse-args
  [args]
  (core/parse-args args))
