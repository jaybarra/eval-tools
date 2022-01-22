(ns eval.test-helpers.core
  (:require
   [clojure.java.shell :as shell :refer [sh]]
   [clojure.math.numeric-tower :as math]))

(defn within?
  "Returns true if two numbers are nearly equal within a given delta.
  Should only be used for 6 significant digits or fewer.

  Examples:
  ```clojure
  (is (true?  (within? 0.1 2.4 2.41)))
  (is (false? (within? 2 10 3)))
  ```"
  [^Double delta a b]
  (<= (- (double (math/abs (- (double a) (double b)))) 1e-7)
      (double delta)))

(defn docker-compose-up
  [dir opts]
  (shell/with-sh-dir dir
    (sh "docker" "compose" "up" "-d"))

  (when-let [health-fn (:wait-fn opts)]
    (health-fn)))

(defn docker-compose-down
  [dir]
  (shell/with-sh-dir dir
    (sh "docker" "compose" "down")))
