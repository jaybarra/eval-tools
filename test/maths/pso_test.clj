(ns maths.pso-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer [facts fact provided throws]]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [maths.pso :as pso])
  (:import (clojure.lang ExceptionInfo)))

(def valid-opts {::pso/omega 1.0
                 ::pso/phi-p 0.8
                 ::pso/phi-s 0.1})

(facts "PSO"
  (fact "validates options"
    (pso/run {}) => (throws ExceptionInfo)
    (pso/run {::pso/opts (merge valid-opts
                                {::max-iterations -1})})
    => (throws ExceptionInfo)
    
    (pso/run {::pso/opts valid-opts})
    =not=> (throws ExceptionInfo)))
