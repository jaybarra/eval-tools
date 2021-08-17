(ns eval.services.cmr.bulk.granule-test
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [eval.cmr.bulk.granule :as bg]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.bulk.granule :as bulk-granule]
   [jsonista.core :as json]))

(deftest submit-job-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ query]
                   (let [job-def (:body query)]
                     (is (= :post (:method query)))
                     (is (= "/ingest/providers/FOO_PROV/bulk-update/granules" (:url query)))
                     (is (spec/valid? ::bg/job job-def)
                         (spec/explain-str ::bg/job job-def))
                     {:status 200
                      :headers {"Content-Type" "application/json"}
                      :body (json/write-value-as-string {:task-id 1 :status 200})}))
                 (-echo-token [_] "mock-token"))
        job {::bg/operation "UPDATE_FIELD"
             ::bg/update-field "S3Link"
             ::bg/updates ["foo-granule-1" "s3://example/foo1"]}]
    (bulk-granule/submit-job! client "FOO_PROV" job)))

(deftest fetch-job-status-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ query]
                   (let [job-def (:body query)]
                     (is (= :get (:method query)))
                     (is (= "/ingest/granule-bulk-update/status/12345" (:url query)))))
                 (-echo-token [_] "mock-token"))]
    (bulk-granule/fetch-job-status client 12345)))

(deftest trigger-status-update-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ query]
                   (let [job-def (:body query)]
                     (is (= :post (:method query)))
                     (is (= "/ingest/granule-bulk-update/status" (:url query)))))
                 (-echo-token [_] "mock-token"))]
    (bulk-granule/trigger-status-update! client)))
