(ns eval.services.cmr.bulk.granule-test
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [eval.cmr.bulk.granule :as bg]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.bulk.granule :as bulk-granule]
   [eval.services.cmr.test-core :as cmr-test]
   [jsonista.core :as json]))

(deftest benchmark-test
  (let [test-client (cmr-test/client
                     (fn [_ query]
                       (is (= "/ingest/granule-bulk-update/status/1" (get query :url))
                           {:status 200}))
                     (constantly "test-echo-token"))
        context (cmr-test/context :test test-client)]
    (bulk-granule/benchmark context :test 1)))

(deftest add-update-instructions-test
  (testing "uses the transformation given for each granule-ur"
    (let [new-job (bulk-granule/add-update-instructions
                   {}
                   ["gran1" "gran2"]
                   (constantly "s3://example.com/bucket"))]
      (is (= [["gran1" "s3://example.com/bucket"]
              ["gran2" "s3://example.com/bucket"]]
             (:updates new-job))))))

(deftest submit-job-test
  (let [test-client (reify cmr/CmrClient
                      (-invoke [_ query]
                        (let [job-def (json/read-value (:body query) json/keyword-keys-object-mapper)]
                          (is (= :post (:method query)))
                          (is (= "/ingest/providers/FOO_PROV/bulk-update/granules" (:url query)))
                          (is (spec/valid? ::bg/job job-def)
                              (spec/explain-str ::bg/job job-def))
                          {:status 200
                           :headers {"Content-Type" "application/json"}
                           :body (json/write-value-as-string {:task-id 1 :status 200})}))
                      (-echo-token [_] "mock-token"))
        context (cmr-test/context :test test-client)
        job {::bg/operation "UPDATE_FIELD"
             ::bg/update-field "S3Link"
             ::bg/updates ["foo-granule-1" "s3://example/foo1"]}]
    (bulk-granule/submit-job! context :test "FOO_PROV" job)))

(deftest fetch-job-status-test
  (let [test-client (reify cmr/CmrClient
                      (-invoke [_ query]
                        (let [job-def (json/read-value (:body query) json/keyword-keys-object-mapper)]
                          (is (= :get (:method query)))
                          (is (= "/ingest/granule-bulk-update/status/12345" (:url query)))))
                      (-echo-token [_] "mock-token"))
        context (cmr-test/context :test test-client)]
    (bulk-granule/fetch-job-status context :test 12345)))

(deftest trigger-status-update-test
  (let [test-client (reify cmr/CmrClient
                      (-invoke [_ query]
                        (let [job-def (json/read-value (:body query) json/keyword-keys-object-mapper)]
                          (is (= :post (:method query)))
                          (is (= "/ingest/granule-bulk-update/status" (:url query)))))
                      (-echo-token [_] "mock-token"))
        context (cmr-test/context :test test-client)]
    (bulk-granule/trigger-status-update! context :test)))
