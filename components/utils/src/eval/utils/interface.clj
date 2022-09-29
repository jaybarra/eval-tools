(ns eval.utils.interface
  (:require
   [eval.utils.core :as core]
   [eval.utils.xml :as xml]))

(def edn->json core/edn->json)

(def remove-nil-keys core/remove-nil-keys)

(def remove-blank-keys core/remove-blank-keys)

(def maps-to core/maps-to)

(def edn->xml xml/edn->xml)
