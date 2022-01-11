(ns eval.es-driver.core
  (:gen-class)
  (:require
   [eval.es-driver.gdelt :as gdelt]
   [eval.es-driver.geojson :as geojson])
  (:import
   [java.time Instant LocalDateTime ZoneOffset]))

(defn -main
  [& args]
  (let [{:keys [elasticsearch]} args
        now (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC)
        week-prior (.minusDays now 7)]
    
    (gdelt/harvest-since elasticsearch week-prior)
    (geojson/create-searchable-index elasticsearch)))

(comment
  (-main {:elasticsearch {:url "http://localhost:9210"}})

  (gdelt/harvest-since
   {:url "http://localhost:9210"}
   (.minusHours (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC) 1))
  )
