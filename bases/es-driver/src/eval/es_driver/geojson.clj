(ns eval.es-driver.geojson
  (:require
   [eval.elastic.interface :as es]))

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

(defn create-searchable-index
  "Creates an index with geo_shape fields."
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
