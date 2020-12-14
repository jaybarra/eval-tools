(ns eval.core-test
  (:require
   [clojure.test :refer :all]
   [eval.core :as core]))

(deftest factorial-test
  (are [input output] (= output (core/factorial input))
    1 1
    2 2
    3 6
    4 24
    5 120))

(deftest fibo-test
  (is (not= nil core/fib 1))

  (is (pos? (core/fib 1)))

  (are [input output] (= output (core/fib input))
    1 1
    2 2
    3 3
    4 5
    5 8
    6 13
    7 21
    20 10946))

(deftest collatz-test
  (is (= 1 (core/collatz 1)))
  (is (= 1 (core/collatz 2)))
  (is (= 1 (core/collatz 3)))

  (is (thrown? AssertionError (core/collatz 0.5)))
  (is (thrown? AssertionError (core/collatz 0)))
  (is (thrown? AssertionError (core/collatz -1))))

(deftest tower-test
  (is (= [["S" "D"]]
         (core/tower 1 "S" "D" "A")))
  (is (= [["S" "A"]
          ["S" "D"]
          ["A" "D"]]
         (core/tower 2 "S" "D" "A")))

  (is (thrown? clojure.lang.ExceptionInfo
               (core/tower 0 "S" "D" "A"))))

