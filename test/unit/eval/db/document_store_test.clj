(ns eval.db.document-store-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.db.document-store :as ds]))

(deftest create-document-store-test
  (let [dbstore (ds/create-document-store {:type :noop})]
    (is (not (nil? dbstore)))

    (.save! dbstore {})
    (.query dbstore {})
    (.halt! dbstore)))

(deftest stop-document-store-test
  (let [dbstore (reify ds/DocumentStore
                  (save! [this document] nil)
                  (query [this query] nil)
                  (halt! [this] (is (true? true) "no exception thrown")))]
    (ds/stop-document-store dbstore)))
