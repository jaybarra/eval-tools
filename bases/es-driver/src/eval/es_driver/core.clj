(ns eval.es-driver.core
  (:gen-class)
  (:require
   [eval.es-driver.gdelt :as gdelt]
   [eval.es-driver.shapes :as shapes])
  (:import
   [java.time Instant LocalDateTime ZoneOffset]))

(defn -main
  [& args]
  (let [{:keys [elasticsearch]} args
        now (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC)
        week-prior (.minusDays now 7)]
    
    (gdelt/harvest-since elasticsearch week-prior)
    (shapes/create-index elasticsearch)
    (shapes/index-cartesian elasticsearch)
    (shapes/index-geodetic elasticsearch)))

(comment
  (-main {:elasticsearch {:url "http://localhost:9210"}})

  (gdelt/harvest-since
   {:url "http://localhost:9210"}
   (.minusHours (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC) 1))

  ;; Put shape data in elasticsearch
  (do
    (shapes/create-index {:url "http://localhost:9210"})
    (shapes/index-cartesian {:url "http://localhost:9210"})
    (shapes/index-cartesian-with-hole {:url "http://localhost:9210"})
    (shapes/index-geodetic {:url "http://localhost:9210"})
    (shapes/index-geodetic-with-hole {:url "http://localhost:9210"}))
  )
