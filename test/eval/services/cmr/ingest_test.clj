(ns eval.services.cmr.ingest-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.services.cmr.ingest :as ingest]
   [eval.services.cmr.test-core :as cmr-test]
   [jsonista.core :as json]))

(deftest upload-collection-test
  (let [test-client (cmr-test/client
                     (fn [_ query]
                       (is (= "/ingest/providers/FOO/collections" (:url query)))
                       (is (= {} (json/read-value (:body query)))))
                     (constantly "test-echo-token"))
        context (cmr-test/context :test test-client)]
    (ingest/upload-collection context :test "FOO" {})))
