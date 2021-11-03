(ns eval.cmr.commands.bulk-granule-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.commands.bulk-granule :as bulk-granule]
   [eval.cmr.core :as cmr]))

(deftest post-job-test
  (let [command (bulk-granule/post-job "foo" {})]
    (is (spec/valid? ::cmr/command command))
    (is (= {:request {:method :post
                      :url "/ingest/providers/foo/bulk-update/granules"
                      :headers {"Content-Type" "application/json"}}}
           (update-in command [:request] dissoc :body)))
    (is (= {}
           (get-in command [:request :body])))))

(deftest trigger-update-test
  (is (spec/valid? ::cmr/command (bulk-granule/trigger-update)))
  (is (= {:request {:method :post
                    :url "/ingest/granule-bulk-update/status"}}
         (bulk-granule/trigger-update))))

(deftest get-job-status-test
  (is (spec/valid? ::cmr/command (bulk-granule/get-job-status 1)))
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
