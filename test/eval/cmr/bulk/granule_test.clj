(ns eval.cmr.bulk.granule-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [eval.cmr.bulk.granule :as bulk-granule]))

(deftest add-update-instructions-test
  (let [job {}]
    (is (= [["gran1" "s3://example.com/bucket"]
            ["gran2" "s3://example.com/bucket"]]
           (:updates
            (bulk-granule/add-update-instructions
             job
             ["gran1" "gran2"]
             (constantly "s3://example.com/bucket")))))))
