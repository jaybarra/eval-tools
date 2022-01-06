(ns eval.elastic.interface
  (:require
   [eval.elastic.core :as core]))

(defn create-index-template
  [conn label template]
  (core/create-index-template conn label template))

(defn create-index
  [conn label index]
  (core/create-index conn label index))

(defn close-index
  [conn index]
  (core/close-index conn index))

(defn delete-index
  [conn index]
  (core/delete-index conn index))

(defn create-document
  [conn index doc & [id]]
  (core/create-document conn index doc id))

(comment
  (create-index {:url "http://localhost:9210"}
                "my-index"
                {:mappings
                 {:properties
                  {:sometext
                   {:type "keyword"}}}})
  (delete-index {:url "http://localhost:9210"} "my-index")
  )
