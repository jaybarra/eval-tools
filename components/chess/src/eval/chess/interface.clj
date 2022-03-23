(ns eval.chess.interface
  (:require
   [clojure.string :as str]))

(defn is-light-square?
  [sq]
  (even? (mod (+ (:file sq) (:rank sq)) 2)))

(defonce base-board (for [file (range 8)
                          rank (range 8)]
                      {:file file
                       :rank rank}))

(defn piece->symbol
  [piece]
  (let [sym (case (:rank piece)
              :king "k"
              :queen "q"
              :bishop "b"
              :knight "n"
              :rook "r"
              :pawn "p")]
    (if (= :white (:color piece))
      (str/upper-case sym)
      sym)))

(defn fen->piece
  [fen]
  (let [rank (case (str/lower-case fen)
               "k" :king
               "q" :queen
               "b" :bishop
               "n" :knight
               "r" :rook
               "p" :pawn)
        color (if (re-find #"[A-Z]" fen) :white :black)]
    {:rank rank
     :color color}))

(defn board 
  [fen]
  (let [[pieces extra] (str/split fen #"\s")]
    (loop [gb (vec base-board)
           pieces (seq (str/replace pieces #"/" ""))
           idx 0]
      (if-not (seq pieces)
        gb
        (if-let [n# (re-find #"\d" (str (first pieces)))]
          (recur gb
                 (rest pieces)
                 (+ idx (Integer/parseInt n#)))
          (recur (update-in gb [idx] assoc :piece (fen->piece (str (first pieces))))
                 (rest pieces)
                 (inc idx)))))))

(defn draw-board!
  [game-board]
  (printf "\n=================================\n")
  (doseq [row (partition 8 game-board)]
    (do
      (printf "|")
      (doseq [sq row]
         (if (is-light-square? sq)
           (printf " %s |" (if-let [piece (:piece sq)] (piece->symbol piece) " "))
           (printf " %s |" (if-let [piece (:piece sq)] (piece->symbol piece) "#"))))
      (printf "\n")))
  (printf "=================================\n")
  )

(comment
  (def fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
  (-> (board fen)
      (draw-board!))
  )
