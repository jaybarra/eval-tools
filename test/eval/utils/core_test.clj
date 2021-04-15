(ns eval.utils.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [eval.utils.core :as utils]))

(deftest spec-validate-test
  (testing "throwing"
    (let [pos-spec (s/def ::pos-num pos?)]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Validation failed"
                            (utils/spec-validate pos-spec -1))))))

(deftest within?-test
  (testing "returns correct evaluations"
    (are [delta a b expected] (= expected (utils/within? delta a b))
      ;; base check
      1 4 5 true
      1 10 20 false
      100 10 20 true

      ;; order doesn't matter for values
      1 4 6 false
      1 6 4 false
      1 5 4 true

      ;; decimals are fine
      0.1 2.1 2.2 true
      0.1 2.1 2.0 true
      0.1 2.3 2.1 false
      0.1 2.32 2.33 true
      0.001 2.32 2.33 false

      ;; small deltas are ok
      1e-6 2.000001 2.000002 true
      1e-6 2.000001 2.000003 false)))
