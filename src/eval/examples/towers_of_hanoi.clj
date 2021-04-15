(ns eval.examples.towers-of-hanoi)

(defn play
  "Tower of Hanoi solver. Returns list of movements."
  [n source dest aux]
  (cond
    (= n 1) [[source dest]]
    (> n 1) (concat (play (dec n) source aux dest)
                    (play 1 source dest aux)
                    (play (dec n) aux dest source))
    :else (throw (ex-info "Values smaller than 1 are not allowed."
                          {:cause "Tower height" n "is invalid"}))))

(defn print-tower!
  "Takes list of tower moves and prints the solving sequence."
  [tower-moves]
  (println "== Tower of Hanoi Solver ==")
  (doseq [[s d] tower-moves] (printf "Move disk from %s to %s\n" s d)))
