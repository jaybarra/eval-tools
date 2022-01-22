(ns eval.user-input.core
  (:require
   [clojure.string :as str]))

(defn named?
  [arg]
  (and (-> arg nil? not)
       (str/includes? arg ":")))

(defn unnamed?
  [arg]
  (and (-> arg nil? not)
       (-> arg named? not)))

(defn extract-named
  [args]
  (filter named? args))

(defn parse-args
  [args]
  {:cmd (-> args first str/lower-case keyword)
   :cmr (-> args second str/lower-case keyword)
   :args (->> args (drop 2) vec)})

(parse-args ["search" "sit" "c" "p:PODAAC"])
