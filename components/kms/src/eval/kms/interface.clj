(ns eval.kms.interface
  (:require
   [eval.kms.core :as core]))

(defn get-scheme
  [kms-root schema & [version]]
  (core/get-scheme kms-root schema version))

(defn available-versions
  [kms-root]
  (core/available-versions kms-root))
