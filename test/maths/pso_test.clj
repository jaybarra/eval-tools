(ns maths.pso-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [maths.pso :as pso :refer [compute-velocity!]])
  (:import (clojure.lang ExceptionInfo)))

(def valid-opts {::pso/max-iterations 100
                 ::pso/omega 1.1
                 ::pso/phi-p 0.8
                 ::pso/phi-s 0.1})

(deftest pso
  (testing "validates options"
    (is (thrown? ExceptionInfo
                 (pso/run {})))
    (is (thrown? ExceptionInfo
                 (pso/run
                   {::pso/opts
                    (merge valid-opts
                           {::pso/max-iterations -1})})))))

(deftest compute-velocity!-test
  (testing "velocity is different from current velocity"
    (is (not= 1.0
              (compute-velocity! valid-opts 1.0 0 0 2)))))
