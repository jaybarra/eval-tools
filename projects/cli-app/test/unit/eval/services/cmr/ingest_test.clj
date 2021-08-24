(ns eval.services.cmr.ingest-test
  (:require
   [clojure.test :refer [deftest is]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.ingest :as ingest]))

(deftest upload-collection-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ query]
                   (is (= "/ingest/providers/FOO/collections" (:url query)))
                   (is (= {} (:body query))))
                 (-token [_] "test-echo-token"))]
    (ingest/upload-collection client "FOO" {})))
