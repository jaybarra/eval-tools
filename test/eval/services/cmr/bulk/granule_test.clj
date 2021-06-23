(ns eval.services.cmr.bulk.granule-test
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.bulk.granule :as bulk-granule]
   [eval.services.cmr.test-core :as cmr-test]
   [clojure.java.io :as io])
  (:import
   [java.nio.file Files Path]))

(deftest benchmark-test
  (let [test-client (cmr-test/client
                     (fn [_ query]
                       (is (= "/ingest/granule-bulk-update/status/1" (get query :url))
                           {:status 200}))
                     (constantly "test-echo-token"))
        context (cmr-test/context :test test-client)]
    (bulk-granule/benchmark context :test 1)))
