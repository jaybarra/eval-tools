(ns eval.services.cmr.ingest-test
  (:require
   [clojure.test :refer [deftest is]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.ingest :as ingest]))

(deftest upload-collection-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ command]
                   (is (= "/ingest/providers/FOO/collections" (get-in command [::cmr/request :url])))
                   (is (= {} (get-in command [::cmr/request :body]))))
                 (-token [_] "test-echo-token"))]
    (ingest/upload-collection client "FOO" {})))
