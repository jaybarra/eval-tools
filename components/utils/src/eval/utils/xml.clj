(ns eval.utils.xml
(:require
 [clojure.data.xml :as xml]
 [clojure.string :as str]))

(defn edn->xml
  "Converts an EDN data structure to an equivalent XML."
  [key-name data]
  (cond
    (= (type data) java.util.Date) (xml/element (name key-name) {} (.toString data))
    (map? data) (xml/element
                 key-name
                 {}
                 (for [k (keys data)]
                   (edn->xml k (get data k))))
    (coll? data) (when (seq data)
                   (apply (partial xml/element
                                   (str (name key-name) (when-not (str/ends-with? (name key-name) "s") "s"))
                                   {:type "array"})
                          (map (partial edn->xml key-name) data)))
    :else (xml/element key-name {} data)))
