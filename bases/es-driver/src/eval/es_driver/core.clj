(ns eval.es-driver.core
  (:require
   [eval.elastic.interface :as es]
   [eval.gdelt.interface.v2 :as gdeltv2])
  (:import
   [java.time Instant LocalDateTime ZoneOffset]
   [java.time.format DateTimeFormatter])
  (:gen-class))

(def geojson
  {:type "FeatureCollection"
   :features
   [{:type "Feature"
     :properties {}
     :geometry
     {:type "Polygon"
      :coordinates
      [[-129.375
        24.206889622398023]
       [-68.5546875
        24.206889622398023]
       [-68.5546875
        50.736455137010665]
       [-129.375
        50.736455137010665]
       [-129.375
        24.206889622398023]]}}]})

(defn create-searchable
  [conn]
  (let [idx "geo-searchables"
        config {:mappings
                {:properties
                 {:geometry {:type "geo_shape"}}}}]
    (es/delete-index conn idx)
    (es/create-index conn idx config)
    (es/create-document conn idx {:name "big square 1"
                                  :moose-per-cubic-decimeter 1024
                                  :geometry {:type "Polygon"
                                             :coordinates
                                             [[[-129.375
                                                24.206889622398023]
                                               [-68.5546875
                                                24.206889622398023]
                                               [-68.5546875
                                                50.736455137010665]
                                               [-129.375
                                                50.736455137010665]
                                               [-129.375
                                                24.206889622398023]]]}})))

(defn most-recent-quarter
  "Returns the most recent quarter hour. 00 15 30 45"
  [^LocalDateTime ldt]
  (* 15 (quot (.getMinute ldt) 15)))

(defn harvest-latest
  "Pulls the latest gdeltv2 events manifest and indexes them in elasticsearch.
   The index is 'gdelt-v2-events-yyyyMMdd' with the corresponding date substituted."
  [conn]
  (let [events (gdeltv2/get-latest-events)
        now (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC)
        date (.format
              (DateTimeFormatter/ofPattern "yyyyMMddhh")
              now)
        minute (most-recent-quarter now)
        datetime (str date minute "00")
        index-label (str "gdelt-v2-events-" datetime)
        index {:mappings
               {:properties
                {:global-event-id {:type "integer"}
                 :dateadded {:type "date"
                             :format "yyyyMMddHHmmss"}
                 :actor1-geo {:properties {:location {:type "geo_point"}}}
                 :actor2-geo {:properties {:location {:type "geo_point"}}}
                 :action-geo {:properties {:location {:type "geo_point"}}}}}}]
    (es/create-index conn index-label index)
    (doseq [event events]
      (es/create-document conn index-label event (:global-event-id event)))))

(defn -main
  [& _args]
  (let [conn {:url "http://localhost:9210"}]
    (harvest-latest conn)
    (create-searchable conn)))

(comment
  (let [conn {:url "http://localhost:9210"}]
    (harvest-latest conn)
    (create-searchable conn)))
