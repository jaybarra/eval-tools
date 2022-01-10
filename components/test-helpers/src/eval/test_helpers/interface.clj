(ns eval.test-helpers.interface
  (:require
   [eval.test-helpers.core :as core]))

(defn within?
  "Returns true if two numbers are nearly equal within a given delta.
  Should only be used for 6 significant digits or fewer.

  Examples:
  ```clojure
  (is (true?  (within? 0.1 2.4 2.41)))
  (is (false? (within? 2 10 3)))
  ```"
  [^Double delta a b]
  (core/within? delta a b))
