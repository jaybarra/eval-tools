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
  "Remove keys mapped to `nil` values in a map."
  [m]
  (reduce dissoc-nil m m))

(defn- dissoc-blank
  [m kv]
  (let [value (val kv)]
    (if (or (nil? value)
            (and (string? value) (str/blank? value)))
      (dissoc m (key kv))
      m)))

(defn remove-blank-keys
  "Remove keys mapped to `nil` or empty string in a map."
  [m]
  (reduce dissoc-blank m m))

(defn maps-to
  "Return a list of key-pairs that map to each other by way of matching values."
  [map-a map-b]
  (for [[a-key a-val] map-a
        [b-key b-val] map-b
        :when (= a-val b-val)]
    [a-key b-key]))
