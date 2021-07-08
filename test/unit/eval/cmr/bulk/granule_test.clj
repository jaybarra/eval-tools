(ns eval.cmr.bulk.granule-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [eval.cmr.bulk.granule :as bulk-granule]
   [eval.cmr.core :as cmr]
   [jsonista.core :as json]))

(deftest post-job-test
  (let [command (bulk-granule/post-job "foo" {})]
    (is (= {:request {:method :post
                      :url "/ingest/providers/foo/bulk-update/granules"
                      :headers {"Content-Type" "application/json"}}}
           (update-in command [:request] dissoc :body)))
    (is (= {}
           (json/read-value (get-in command [:request :body]))))))

(deftest trigger-update-test
  (is (= {:request {:method :post
                    :url "/ingest/granule-bulk-update/status"}}
         (bulk-granule/trigger-update))))

(deftest get-job-status-test
  (testing "without options"
    (let [command (bulk-granule/get-job-status 3)]
      (is (= {:request {:method :get
                        :url "/ingest/granule-bulk-update/status/3"}}
             command))))
  (testing "with options"
    (let [command (bulk-granule/get-job-status 3 {:show-granules true})]
      (is (= {:request {:method :get
                        :url "/ingest/granule-bulk-update/status/3"
                        :query-params {"show_granules" true}}}
             command)))))