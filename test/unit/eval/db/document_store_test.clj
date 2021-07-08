(ns eval.db.document-store-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.db.document-store :as ds]))

(deftest create-document-store-test
  (is (not (nil? (ds/create-document-store {:type :noop})))))
