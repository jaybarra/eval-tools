(ns eval.examples.towers-of-hanoi-test
  (:require
   [clojure.test :refer :all]
   [eval.examples.towers-of-hanoi :as tower]))

(deftest play-test
  (is (= [["S" "D"]]
         (tower/play 1 "S" "D" "A")))
  (is (= [["S" "A"]
          ["S" "D"]
          ["A" "D"]]
         (tower/play 2 "S" "D" "A")))

  (is (thrown? clojure.lang.ExceptionInfo
               (tower/play 0 "S" "D" "A"))))
