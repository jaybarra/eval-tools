(ns eval.utils.core
  (:require
   [jsonista.core :as json]))

(defn edn->json
  [edn]
  (json/write-value-as-string edn))
