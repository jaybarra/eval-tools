(ns eval.path-finder.interface
  (:require
   [eval.path-finder.core :as core]))

(defn generate-path
  [points]
  (core/generate-path points))
