(ns eval.gdelt.interface.v2
  (:require 
   [eval.gdelt.v2 :as gdeltv2]))

(defn get-latest-manifest
  "Retrieves the latest manifest from the gdelt server."
  []
  (gdeltv2/get-latest-manifest))

(defn events
  [manifest]
  (gdeltv2/events manifest))
