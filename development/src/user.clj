(ns user
  (:require
   [clojure.java.io :as io]
   [clojure.tools.namespace.repl :refer [refresh]]))

(defn banner
  "Print the banner."
  []
  (when-let [banner (io/resource "banner.txt")]
    (println (slurp banner))))

(defn start
  []
  (refresh))

(defn stop
  [])

(defn reset
  []
  (refresh))
