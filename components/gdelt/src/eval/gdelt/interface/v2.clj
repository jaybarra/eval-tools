(ns eval.gdelt.interface.v2
  (:require 
   [eval.gdelt.v2 :as gdeltv2]))

(defn get-latest-events
  "Retrieves the latest events from the gdelt server."
  []
  (gdeltv2/get-latest-events))

(defn get-events
  "Retrieves the events from the gdelt server for the given datetime.
   GDelt V2 updates every 15 minutes
   Datetime format requies yyyyMMddhhmmss where `mm` is #{00 15 30 45} and `ss` is always 00
   ```clojure
   (get-events \"2020\" \"01\" \"03\" \"00\" \"15\")
   (get-events \"20200103001500\")
   ```
   "
  ([yyyy MM dd hh mm & [_ss]]
   (gdeltv2/get-events (str yyyy MM dd hh mm "00")))
  ([datetime]
   (gdeltv2/get-events datetime)))
