(ns eval.cmr.bulk.granule-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [eval.cmr.bulk.granule :as bulk-granule]
   [eval.cmr.core :as cmr]
   [jsonista.core :as json]))

(deftest post-job-test
  (let [{:keys [body] :as req} (bulk-granule/post-job "foo" {})]
    (is (= {:method :post
            :url "/ingest/providers/foo/bulk-update/granules"
            :headers {"Content-Type" "application/json"}}
           (dissoc req :body)))
    (is (= {}
           (json/read-value body)))))

(deftest trigger-update-test
  (is (= {:method :post
          :url "/ingest/granule-bulk-update/status"}
         (bulk-granule/trigger-update))))

(deftest get-job-status-test
  (testing "without options"
    (let [req (bulk-granule/get-job-status 3)]
      (is (= {:method :get
              :url "/ingest/granule-bulk-update/status/3"}
             req))))
  (testing "with options"
    (let [req (bulk-granule/get-job-status 3 {:show_granules true})]
      (is (= {:method :get
              :url "/ingest/granule-bulk-update/status/3"
              :query-params {:show_granules true}}
             req)))))
