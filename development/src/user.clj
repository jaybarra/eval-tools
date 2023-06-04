(ns user
  (:require
   [clojure.java.io :as io]
   [clojure.tools.namespace.repl :refer [refresh]]))

(defn print-banner
  []
  (let [title "EVAL-TOOLS"
        padding (- 80 (count title))
        lpad (quot padding 2)
        rpad (- 77 lpad 3)]
    (if-let [banner (io/resource "banner.txt")]
      (println (slurp banner))
      (printf (str (apply str (repeat 80 "*"))
                   (str "%n***%" lpad "s")
                   (str (str/join (repeat rpad " ")) "***%n")
                   (apply str (repeat 80 "*"))
                   "%n")
              title))))

(comment
  (print-banner)
  )
