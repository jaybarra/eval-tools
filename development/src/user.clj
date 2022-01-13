(ns user
  (:require
   [clojure.java.io :as io]))

(defn banner
  "Print the banner."
  []
  (when-let [banner (io/resource "banner.txt")]
    (println (slurp banner))))
