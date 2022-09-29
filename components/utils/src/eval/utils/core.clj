(ns eval.utils.core
  (:require
   [clojure.string :as str]
   [jsonista.core :as json]))

(defn edn->json
  [edn]
  (json/write-value-as-string edn))

(defn- dissoc-nil
  [m kv]
  (if (nil? (val kv))
    (dissoc m (key kv))
    m))

(defn remove-nil-keys
  "Removes keys mapping to nil values in a map."
  [m]
  (reduce dissoc-nil m m))

(defn- dissoc-blank
  [m kv]
  (if (or (nil? (val kv))
          (and (string? (val kv))
               (str/blank? (val kv))))
    (dissoc m (key kv))
    m))

(defn remove-blank-keys
  "Removes keys mapping to nil or blank values in a map."
  [m]
  (reduce dissoc-blank m m))

(defn maps-to
  "Return a list of key-pairs that map to each other by way of matching values."
  [a b]
  (for [[ak av] a
        [bk bv] b
        :when (= av bv)]
    [ak bk]))
