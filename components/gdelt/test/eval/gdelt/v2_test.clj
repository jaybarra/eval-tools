(ns eval.gdelt.v2-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is are]]
   [eval.gdelt.v2 :as gdelt]))

(deftest datetime-validation--exceptions
  (are [dt] (false? (spec/valid? ::gdelt/datetime dt))
    "2020"
    "20209901000000" ; invalid month
    "20201301000000" ; invalid month
    "20201251000000" ; invalid day
    "20201251550000" ; invalid hour
    "20201251007000" ; invalid minutes
    "20201251000100" ; invalid minutes
    "20201251000001" ; invalid seconds
    ))

(deftest datetime-validation--valid
  (are [dt] (true? (spec/valid? ::gdelt/datetime dt))
    "20200101000000" ; 00 minute
    "20200101001500" ; 15 minute
    "20200101003000" ; 30 minute
    "20200101004500" ; 45 minute
    ))
