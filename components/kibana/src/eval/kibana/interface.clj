(ns eval.kibana.interface
  (:require
   [eval.kibana.core :as core]))

(defn submit-saved-object
  [conn saved-object]
  (core/submit-saved-object conn saved-object))
