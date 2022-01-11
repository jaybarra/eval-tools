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

(comment
  (create-index {:url "http://localhost:9210"}
                "my-index"
                {:mappings
                 {:properties
                  {:sometext
                   {:type "keyword"}}}})
  (delete-index {:url "http://localhost:9210"} "my-index")
  )
