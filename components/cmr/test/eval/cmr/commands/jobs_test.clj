(ns eval.cmr.commands.jobs-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.client :as cmr]
   [eval.cmr.commands.jobs :as jobs]))

(deftest cleanup-expired-collections-test
  (is (spec/valid? ::cmr/command (jobs/cleanup-expired-collections)))
  (is (= {:method :post
          :url "/ingest/jobs/cleanup-expired-collections"}
         (::cmr/request (jobs/cleanup-expired-collections)))))

(deftest trigger-full-collection-granule-aggregate-cache-refresh-test
  (is (spec/valid? ::cmr/command (jobs/trigger-full-collection-granule-aggregate-cache-refresh)))
  (is (= {:method :post
          :url "/ingest/jobs/trigger-full-collection-granule-aggregate-cache-refresh"}
         (::cmr/request (jobs/trigger-full-collection-granule-aggregate-cache-refresh)))))

(deftest trigger-partial-collection-granule-aggregate-cache-refresh-test
  (is (spec/valid? ::cmr/command (jobs/trigger-partial-collection-granule-aggregate-cache-refresh)))
  (is = ({:method :post
          :url "/ingest/jobs/trigger-partial-collection-granule-aggregate-cache-refresh"}
         (::cmr/request (jobs/trigger-partial-collection-granule-aggregate-cache-refresh)))))

(deftest trigger-granule-task-cleanup-test
  (is (spec/valid? ::cmr/command (jobs/trigger-granule-task-cleanup)))
  (is (= {:method :post
          :url "/ingest/jobs/trigger-granule-task-cleanup-job"}
         (::cmr/request (jobs/trigger-granule-task-cleanup)))))
