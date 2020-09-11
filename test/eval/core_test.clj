(ns eval.core-test
  (:require
   [clojure.test :refer :all]
   [eval.core :as core]))

(deftest fibo-test
  (is (not= nil core/fib 1))
  (is (pos? (core/fib 1))))

(deftest collatz-test
  (is (= 1 (core/collatz 1)))
  (is (= 1 (core/collatz 2)))
  (is (= 1 (core/collatz 3)))
  #_(is (= 1 (core/collatz 23541561999213451235415619992135234124123))))

(deftest tower-test
  (is (= [["S" "D"]]
         (core/tower 1 "S" "D" "A")))
  (is (= [["S" "A"]
          ["S" "D"]
          ["A" "D"]]
         (core/tower 2 "S" "D" "A"))))

