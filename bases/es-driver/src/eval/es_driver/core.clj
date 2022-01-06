(ns eval.es-driver.core
  (:require
   [clojure.string :as str]
   [eval.elastic.interface :as es]
   [eval.gdelt.interface.v2 :as gdeltv2])
  (:gen-class))

(defn harvest-latest
  [conn]
  (let [manifest (gdeltv2/get-latest-manifest)
        index-label (str "gdelt-v2-events-" (str/join (take 8 (seq (:id manifest)))))
        index {:mappings
               {:properties
                {:global-event-id {:type "integer"}
                 :dateadded {:type "date"
                             :format "yyyyMMddHHmmss"}
                 :actor1-geo {:properties {:location {:type "geo_point"}}}
                 :actor2-geo {:properties {:location {:type "geo_point"}}}
                 :action-geo {:properties {:location {:type "geo_point"}}}}}}]
    (es/create-index conn index-label index)
    (doseq [event (gdeltv2/events manifest)]
      (es/create-document conn index-label event (:global-event-id event)))))

(defn -main
  [& args]
  (harvest-latest {:url "http://localhost:9210"}))

(comment
  (harvest-latest {:url "http://localhost:9210"}))
