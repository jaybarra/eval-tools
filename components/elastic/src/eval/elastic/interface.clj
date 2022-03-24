(ns eval.elastic.interface
  (:require
   [eval.elastic.core :as core]))

(defn create-index-template
  [conn label template]
  (core/create-index-template conn label template))

(defn create-index
  [conn label index & [opts]]
  (core/create-index conn label index opts))

(defn close-index
  [conn index]
  (core/close-index conn index))

(defn delete-index
  [conn index]
  (core/delete-index conn index))

(defn index-document
  [conn index doc & [id]]
  (core/index-document conn index doc id))

(defn bulk-index
  [conn index docs & [opts]]
  (core/bulk-index conn index docs opts))

(defn delete-by-query
  [conn index query & [opts]]
  (core/delete-by-query conn index query opts))
