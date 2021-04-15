(ns eval.core-test
  (:require
   [clojure.test :refer :all]
   [eval.core :as core]))

(deftest factorial-test
  (testing "Simple values"
    (are [input output] (= output (core/factorial input))
      1 1
      2 2
      3 6
      4 24
      5 120)))

(deftest fibonacci-test
  (testing "Known outputs of fibonacci"
    (are [input output] (= output (core/fibonacci input))
      1 1
      2 2
      3 3
      4 5
      5 8
      6 13
      7 21
      20 10946)))

(deftest collatz-test
  (testing "Base cases"
    (is (= 1 (core/collatz 1)))
    (is (= 1 (core/collatz 2)))
    (is (= 1 (core/collatz 3))))

  (testing "Exceptions are handled"
    (is (thrown? AssertionError (core/collatz 0.5)))
    (is (thrown? AssertionError (core/collatz 0)))
    (is (thrown? AssertionError (core/collatz -1)))))
