(ns eval.cmr.bulk.granule-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [eval.cmr.bulk.granule :as bulk-granule]
   [eval.cmr.core :as cmr]
   [jsonista.core :as json]))

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
                          (is (spec/valid? ::bulk-granule/job job-def)
                              (spec/explain-str ::bulk-granule/job job-def))
                          {:status 200
                           :headers {"Content-Type" "application/json"}
                           :body (json/write-value-as-string {:task-id 1 :status 200})}))
                      (-echo-token [_] "mock-token"))
        job {::bulk-granule/operation "UPDATE_FIELD"
             ::bulk-granule/update-field "S3Link"
             ::bulk-granule/updates ["foo-granule-1" "s3://example/foo1"]}]
    (bulk-granule/submit-job! test-client "FOO_PROV" job)))
