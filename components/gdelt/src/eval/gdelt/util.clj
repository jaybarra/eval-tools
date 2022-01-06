(ns eval.gdelt.util
  (:require
   [clojure.java.io :as io])
  (:import
   [java.util.zip ZipInputStream]))

(defn unzip!
  "Unpacks a zip input stream into its file."
  [^ZipInputStream is]
  (loop [ze (.getNextEntry is)]
    (when ze
      (io/copy is (io/file (.getName ze)))
      (recur (.getNextEntry is)))))

(defn gdelt-zip->tsv!
  "Takes a ZipInputStream and returns lines from the TSV as a vector."
  [^ZipInputStream is]
  (when (.getNextEntry is)
    (reduce conj [] (line-seq (io/reader is)))))
