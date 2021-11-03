(ns eval.cmr.commands.jobs-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.commands.jobs :as jobs]))

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
