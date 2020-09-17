(ns eval.core)

;; Factorial
(defn factorial
  [n]
  (let [big-n (bigint n)]
    (if (pos? big-n)
      (* big-n (factorial (dec big-n)))
      1)))

;; Fibonacci
(def fib
  (memoize
    (fn [n]
      (let [big-n (bigint n)]
        (if (< big-n 2)
          1
          (+ (fib (dec (dec n))) (fib (dec n))))))))

;; Collatz Conjecture
(def collatz
  (memoize
    (fn [n]
      (let [big-n (bigint n)]
        (if (= 1 big-n)
          1
          (collatz
            (if (even? big-n)
              (/ big-n 2)
              (/ (inc (* 3 big-n)) 2))))))))

(defn tower
  "Tower of Hanoi solver. Returns list of movements."
  [n source dest aux]
  (cond
    (= n 1) [[source dest]]
    (> n 1) (concat (tower (dec n) source aux dest)
                    (tower 1 source dest aux)
                    (tower (dec n) aux dest source))
    :else (throw (ex-info "Values smaller than 1 are not allowed."
                          {:cause "Tower height" n "is invalid"}))))

(defn print-tower!
  "Takes list of tower moves and prints the solving sequence."
  [tower-moves]
  (println "== Tower of Hanoi Solver ==")
  (doseq [[s d] tower-moves] (printf "Move disk from %s to %s\n" s d)))

