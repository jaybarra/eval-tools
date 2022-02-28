(ns eval.utils.interface
  (:require
   [eval.utils.core :as core]
   [eval.utils.xml :as xml]))

(defn edn->json
  [edn]
  (core/edn->json edn))

(defn remove-nil-keys
  [m]
  (core/remove-nil-keys m))

(defn remove-blank-keys
  [m]
  (core/remove-blank-keys m))

(defn edn->xml
  [key-name data]
  (xml/edn->xml key-name data))
