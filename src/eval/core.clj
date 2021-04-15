(ns eval.core
  (:require
   [clojure.spec.alpha :as spec]))

(defn factorial
  "Computes the factorial of the given integer."
  [n]
  (let [big-n (bigint n)]
    (if (pos? big-n)
      (* big-n (factorial (dec big-n)))
      1)))

(defn fibonacci
  "Computes the nth fibonacci number."
  [n]
  (let [big-n (bigint n)]
    (if (< big-n 2)
      1
      (+ (fibonacci (dec (dec n))) (fibonacci (dec n))))))

(defn collatz
  "Computes the collatz value for the given positive integer."
  [n]
  {:pre [(spec/valid? (spec/and pos?) n)]}
  (let [big-n (bigint n)]
    (if (= 1 big-n)
      1
      (collatz
       (if (even? big-n)
         (/ big-n 2)
         (/ (inc (* 3 big-n)) 2))))))
