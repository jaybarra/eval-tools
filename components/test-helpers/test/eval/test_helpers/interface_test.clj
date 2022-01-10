(ns eval.test-helpers.interface-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.test-helpers.interface :as test-helpers]))

(deftest within?-test
  (testing "within checks values within a delta distance correctly"
    (are [delta a b expected] (= expected (test-helpers/within? delta a b))
      ;; base check
      1.0 4 5 true
      1.0 10 20 false
      100.0 10 20 true

      ;; order doesn't matter for values
      1.0 4 6 false
      1.0 6 4 false
      1.0 5 4 true

      ;; decimals are fine
      0.1 2.1 2.2 true
      0.1 2.1 2.0 true
      0.1 2.3 2.1 false
      0.1 2.32 2.33 true
      0.001 2.32 2.33 false

      ;; small deltas are ok
      1e-6 2.000001 2.000002 true
      1e-6 2.000001 2.000003 false)))
