;;; word_path.clj --- word transformation pathfinding
;;; Commentary:

;; Given a start word, an end word, and a dictionary of valid words
;; find the shortest transformation sequence from start to ends
;; such that only one letter is changed at each step of the sequence,
;; and each transformed word exists in the dictionary. If there is no
;; possible transformation return nil. Each word in the dictionary
;; has the seme length as start and end and is lowercase.

;; For example

;;; Code:
(ns eval.examples.word-path
  (:require
   [clojure.string :as string]))

(defrecord Node [value left right])
;; sort into tree
;; depth first search
