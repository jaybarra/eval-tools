(ns eval.cmr.commands.index-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.commands.index :as index]
   [eval.cmr.core :as cmr]))

(deftest reindex-provider-collections-test
  (is (spec/valid? ::cmr/command (index/reindex-provider-collections))))

(deftest reindex-all-collections-test
  (is (spec/valid? ::cmr/command (index/reindex-all-collections)))
  (is (= {:method :post
          :url "/ingest/jobs/reindex-all-collections"}
         (::cmr/request (index/reindex-all-collections))))
  (testing "with forcing updates to version"
    (is (= {:method :post
            :url "/ingest/jobs/reindex-all-collections"
            :query-params {:force_version "true"}}
           (::cmr/request (index/reindex-all-collections true))))))

(deftest reindex-collection-permitted-groups-test
  (is (spec/valid? ::cmr/command (index/reindex-collection-permitted-groups)))
  (is (= {:method :post
          :url "/ingest/jobs/reindex-collection-permitted-groups"}
         (::cmr/request (index/reindex-collection-permitted-groups)))))

(deftest reindex-autocomplete-suggestions-test
  (is (spec/valid? ::cmr/command (index/reindex-autocomplete-suggestions)))
  (is (= {:method :post
          :url "/ingest/jobs/reindex-autocomplete-suggestions"}
         (::cmr/request (index/reindex-autocomplete-suggestions)))))
