(ns eval.utils.core
  (:require
   [clojure.math.numeric-tower :as math]
   [clojure.spec.alpha :as s]))

(s/def ::positive-num pos?)

(defn spec-problems
  [spec value]
  (:clojure.spec.alpha/problems (s/explain-data spec value)))

(defn spec-validate
  "Check spec by value.
  Returns nil on pass, otherwise throws an exception"
  [spec value]
  (when-let [problem (->> (spec-problems spec value)
                          first
                          :via)]
    (throw (ex-info "Validation failed" {:validation problem}))))

(defn within?
  "Returns true if two numbers are nearly equal within a given delta.
  Should only be used for 6 significant digits or fewer.

  Examples:
  ```clojure
  (is (true?  (within? 0.1 2.4 2.41)))
  (is (false? (within? 2 10 3)))
  ```"
  [^Double delta a b]
  (spec-validate ::positive-num delta)
  (<= (- (double (math/abs (- (double a) (double b)))) 1e-7)
      (double delta)))
