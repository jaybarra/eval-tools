(ns eval.services.cmr.ingest-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.ingest :as ingest]
   [jsonista.core :as json]))

(deftest upload-collection-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ query]
                   (is (= "/ingest/providers/FOO/collections" (:url query)))
                   (is (= {} (json/read-value (:body query)))))
                 (-echo-token [_] "test-echo-token"))]
    (ingest/upload-collection client "FOO" {})))
