(ns eval.examples.towers-of-hanoi-test
  (:require
   [clojure.test :refer :all]
   [eval.examples.towers-of-hanoi :as tower :refer [->Move]]))

(deftest play-test
  (is (= [(->Move :left :right)]
         (tower/play 1 :left :right :mid)))
  (is (= [(->Move :left :mid)
          (->Move :left :right)
          (->Move :mid :right)]
         (tower/play 2 :left :right :mid)))

  (is (thrown? clojure.lang.ExceptionInfo
               (tower/play 0 :left :right :mid))))

(deftest print-moves!-test
  (testing "function will not break with default input"
    (is (nil? (tower/print-moves! (tower/play 3 :left :right :mid))))))
