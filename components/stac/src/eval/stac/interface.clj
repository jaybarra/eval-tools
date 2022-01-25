(ns eval.stac.interface
  (:require
   [eval.stac.core :as core]))

(defn get-catalog
  [url]
  (core/get-catalog url))

(defn children
  [catalog]
  (core/children catalog))

(defn next-page
  [catalog]
  (core/next-page catalog))

(defn prev-page
  [catalog]
  (core/prev-page catalog))

(defn validate
  [catalog]
  (core/validate catalog))
