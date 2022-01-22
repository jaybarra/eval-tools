(ns eval.gdelt.util
  "Utilities for GDelt"
  (:require
   [clojure.java.io :as io])
  (:import
   [java.util.zip ZipInputStream]))

(defn gdelt-zip->tsv
  "Takes a ZipInputStream and returns lines from the TSV as a vector."
  [^ZipInputStream zis]
  (when (.getNextEntry zis)
    (reduce conj [] (line-seq (io/reader zis)))))
