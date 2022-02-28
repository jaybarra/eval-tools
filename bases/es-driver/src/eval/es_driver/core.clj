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
    (shapes/ingest-geojson elasticsearch shapes/shapes)))

(comment
  (-main {:elasticsearch {:url "http://localhost:9210"}})

  (gdelt/harvest-since
   {:url "http://localhost:9209"}
   (.minusDays (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC) 2))
  )
