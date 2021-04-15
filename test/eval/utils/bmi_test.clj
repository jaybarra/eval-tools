(ns eval.utils.bmi-test
  (:require
   [clojure.test :refer :all]
   [eval.utils.bmi :as bmi]
   [eval.utils.core :refer [within?]]))

(deftest calculate-bmi-test
  (testing "BMI calculations"
    (are [height weight result]
        (within? 0.5
                 result
                 (bmi/calculate-bmi height weight))

      ;; height weight BMI
      152.4 47.1 20
      152.4 59.1 25
      152.4 75.0 32
      152.4 93.2 40)))

(deftest bmi-status-test
  (testing "classify group by bmi"
    (is (= :underweight (bmi/bmi-status 17.0)))
    (is (= :underweight (bmi/bmi-status 18.5)))
    (is (= :normal (bmi/bmi-status 18.6)))
    (is (= :normal (bmi/bmi-status 24.9)))
    (is (= :overweight (bmi/bmi-status 25.0)))
    (is (= :overweight (bmi/bmi-status 29.9)))
    (is (= :obese (bmi/bmi-status 30.0)))
    (is (= :obese (bmi/bmi-status 400.0)))))
