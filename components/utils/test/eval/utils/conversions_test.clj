(ns eval.utils.conversions-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.test-helpers.interface :refer [within?]]
   [eval.utils.conversions :as conversions]))

(deftest kg->lb-test
  (are [mass expected]
       (is (within? 0.001
                    expected
                    (conversions/kg->lb mass)))
    0 0
    1.0 2.2046
    -1.0 -2.2046
    10.0 22.0462
    100.0 220.4622))

(deftest lb->kg-test
  (are [mass expected]
       (is (within? 0.001
                    expected
                    (conversions/lb->kg mass)))
    0 0
    2.2046 1.0
    -2.2046 -1.0
    22.0462 10.0
    220.4622 100.0))

(deftest ft->mile-test
  (are [ft m] (= m (conversions/ft->mile ft))
    5280 1.0
    2640 0.5
    10560 2.0))

(deftest mile->ft-test
  (are [m ft] (= ft (conversions/mile->ft m))
    1.0 5280.0
    0.5 2640.0
    2.0 10560.0))

(deftest in->ft-test
  (are [in ft] (= ft (conversions/in->ft in))
    12 1.0
    6 0.5
    24 2.0))

(deftest ft->in-test
  (are [ft in] (= in (conversions/ft->in ft))
    1 12.0
    0.5 6.0
    2 24.0))

(deftest cm->in-test
  (are [cm in] (within? 0.0001 in (conversions/cm->in cm))
    1 0.3937
    2 0.7874
    3 1.1811
    4 1.5748))

(deftest in->cm-test
  (are [in cm] (within? 0.0001 cm (conversions/in->cm in))
    1 2.54
    2 5.08
    3 7.62
    4 10.16))
