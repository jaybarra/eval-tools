(ns eval.es-driver.core
  (:require
   [eval.es-driver.gdelt :as gdelt]
   [eval.es-driver.geojson :as geojson])
  (:gen-class))

(defn -main
  [& args]
  (let [{:keys [elasticsearch]} args]
    (gdelt/harvest-since elasticsearch "20200101000000")
    (geojson/create-searchable-index elasticsearch)))

(comment
  (-main {:elasticsearch {:url "http://localhost:9210"}})
  
  (gdelt/harvest-since {:url "http://localhost:9200"} "20200101000000"))
