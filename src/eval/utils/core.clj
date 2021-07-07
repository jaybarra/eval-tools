(ns eval.utils.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.math.numeric-tower :as math]))

(set! *warn-on-reflection* true)

(s/def ::positive-num (comp not neg?))

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
  Eg (within? 0.1 2.4 2.41) => true"
  [^Double delta a b]
  (spec-validate ::positive-num delta)
  (<= (- (double (math/abs (- (double a) (double b)))) 1e-7)
      (double delta)))
