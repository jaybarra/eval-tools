(ns eval.examples.towers-of-hanoi)

(defrecord Move [from to])

(defn play
  "Tower of Hanoi solver. Returns list of movements."
  [height source dest aux]
  (cond
    (= height 1) [(->Move source dest)]
    (> height 1) (concat (play (dec height) source aux dest)
                         (play 1 source dest aux)
                         (play (dec height) aux dest source))
    :else (throw (ex-info "Values smaller than 1 are not allowed."
                          {:cause "Tower height" height "is invalid"}))))

(defn print-moves!
  "Takes list of tower moves and prints the solving sequence."
  [moves]
  (println "== Tower of Hanoi Solver ==")
  (doseq [m moves]
    (printf "Move disk from %s to %s\n" (:from m) (:to m))))
