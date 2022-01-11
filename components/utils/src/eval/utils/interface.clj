(ns eval.utils.interface
  (:require
   [eval.utils.core :as core]))

(defn edn->json
  [edn]
  (core/edn->json edn))
