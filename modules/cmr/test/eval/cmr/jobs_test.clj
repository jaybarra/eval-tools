(ns eval.cmr.jobs-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.jobs :as jobs]))

(deftest reindex-all-collections-test
  (is (= {:request
          {:method :post
           :url "/ingest/jobs/reindex-all-collections"}}
         (jobs/reindex-all-collections)))
  (testing "with forcing updates to version"
    (is (= {:request
            {:method :post
             :url "/ingest/jobs/reindex-all-collections"
             :query-params {:force_version "true"}}}
           (jobs/reindex-all-collections true)))))

(deftest reindex-collection-permitted-groups-test
  (is (= {:request
          {:method :post
           :url "/ingest/jobs/reindex-collection-permitted-groups"}}
         (jobs/reindex-collection-permitted-groups))))

(deftest reindex-autocomplete-suggestions-test
  (is (= {:request
          {:method :post
           :url "/ingest/jobs/reindex-autocomplete-suggestions"}}
         (jobs/reindex-autocomplete-suggestions))))

(deftest cleanup-expired-collections-test
  (is (= {:request
          {:method :post
           :url "/ingest/jobs/cleanup-expired-collections"}}
         (jobs/cleanup-expired-collections))))

(deftest trigger-full-collection-granule-aggregate-cache-refresh-test
  (is (= {:request
          {:method :post
           :url "/ingest/jobs/trigger-full-collection-granule-aggregate-cache-refresh"}}
         (jobs/trigger-full-collection-granule-aggregate-cache-refresh))))

(deftest trigger-partial-collection-granule-aggregate-cache-refresh-test
  (is (= {:request
          {:method :post
           :url "/ingest/jobs/trigger-partial-collection-granule-aggregate-cache-refresh"}}
         (jobs/trigger-partial-collection-granule-aggregate-cache-refresh))))

(deftest trigger-granule-task-cleanup-test
  (is (= {:request
          {:method :post
           :url "/ingest/jobs/trigger-granule-task-cleanup-job"}}
         (jobs/trigger-granule-task-cleanup))))
