(ns eval.cmr.bulk.granule-test
  (:require
   [clojure.test :refer :all]
   [eval.cmr.bulk.granule :as bulk-granule]
   [eval.cmr.core :as cmr]
   [eval.cmr.core-test :refer [cmr-handler]]
   [reitit.ring :as ring]
   [ring.adapter.jetty :as jetty]
   [ring.util.response :refer [response status]]))

(defn mock-cmr-fixture
  [f]
  (let [server (jetty/run-jetty cmr-handler {:port 18000 :join? false})]
    (try
      (f)
      (finally
        (.stop server)))))

(use-fixtures :once mock-cmr-fixture)

(deftest add-update-instructions-test
  (is (= {:updates [["ur" "https://example.com/ur"]]}
         (select-keys (bulk-granule/add-update-instructions
                       nil
                       ["ur"]
                       (fn [x] (str "https://example.com/" x)))
                      [:updates]))))

(deftest ^:functional fetch-job-status-test
  (let [cmr-conn (cmr/cmr-conn :local {:port 18000})]
    (testing "it handles strings as job id"
      (is (not (nil? (bulk-granule/fetch-job-status cmr-conn "1")))))

    (testing "it handles integers as job id"
      (is (not (nil? (bulk-granule/fetch-job-status cmr-conn 1)))))))

(deftest ^:functional benchmark-processing-test
  (let [cmr-conn (cmr/cmr-conn :local {:port 18000})]
    (is (map? (bulk-granule/benchmark-processing cmr-conn 1 0)))))

(deftest ^:functional trigger-status-udpate-test
  (let [cmr-conn (cmr/cmr-conn :local {:port 18000})]
    (is (nil? (bulk-granule/trigger-status-update! cmr-conn)))))

(deftest ^:functional submit-job-test
  (let [cmr-conn (cmr/cmr-conn :local {:port 18000})]
    (is (nil? (bulk-granule/submit-job! cmr-conn "TEST_PROV" {})))))
