(ns eval.pso-test
  (:require
   [clojure.test :refer :all]
   [eval.pso :as pso :refer [compute-velocity!]])
  (:import (clojure.lang ExceptionInfo)))

(def valid-opts {::pso/max-iterations 100
                 ::pso/omega 1.1
                 ::pso/phi-p 0.8
                 ::pso/phi-s 0.1})

(deftest pso
  (testing "validates options"
    (is (thrown? ExceptionInfo
                 (pso/run-optimization {})))
    (is (thrown? ExceptionInfo
                 (pso/run-optimization
                  {::pso/opts
                   (merge valid-opts
                          {::pso/max-iterations -1})})))))

(deftest compute-velocity!-test
  (testing "velocity is different from current velocity"
    (is (not= 1.0
              (compute-velocity! valid-opts 1.0 0 0 2)))))

(deftest compute-fitness-test
  (testing "any valid output will be a number"
    (is (instance? Number (pso/compute-fitness [1 0])))))

(deftest sort-particles-by-pos-test
  (let [p1 {::pso/pos [0 0]}
        p2 {::pso/pos [1 1]}]
    (is (= [p2 p1] (pso/sort-particles-by-pos [p1 p2])))))

(deftest sort-particles-by-best-pos-test
  (let [p1 {::pso/best-pos [0 0]}
        p2 {::pso/best-pos [1 1]}]
    (is (= [p2 p1] (pso/sort-particles-by-best-pos [p1 p2])))))
