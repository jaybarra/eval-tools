(ns user
  (:require
   [clojure.java.io :as io]
   [clojure.tools.namespace.repl :refer [refresh]]))

(defn print-banner
  []
  (let [title "EVAL-TOOLS"
        padding (- 80 (count title))
        l#pad (quot padding 2)
        r#pad (- 77 l#pad 3)]
    (if-let [banner (io/resource "banner.txt")]
      (println (slurp banner))
      (printf (str (apply str (repeat 80 "*"))
                   (str "%n***%" l#pad "s")
                   (str (apply str (repeat r#pad " ")) "***%n")
                   (apply str (repeat 80 "*"))
                   "%n")
              title))))

(comment
  (print-banner)
  )
