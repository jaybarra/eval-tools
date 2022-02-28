(ns eval.user-input.interface
  (:require
   [eval.user-input.core :as core]))

(defn parse-params
  [args]
  (core/parse-params args #{}))
