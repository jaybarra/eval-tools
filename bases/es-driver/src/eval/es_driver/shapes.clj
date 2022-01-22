(ns eval.es-driver.shapes
  (:require
   [eval.elastic.interface :as es]))

(def idx-name "shape-searchables")

(defn create-index
  "Creates an index 'shape-searchables' with 'geo_shape' and 'shape' fields."
  [conn]
  (let [idx idx-name
        config {:mappings
                {:properties
                 {:geodetic {:type "geo_shape"}
                  :cartesian {:type "shape"}}}}]
    (es/delete-index conn idx)
    (es/create-index conn idx config)))

(defn index-geodetic
  [conn]
  (es/index-document
   conn
   idx-name
   {:name "geodetic square over the US"
    :geodetic {:type "Polygon"
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
                  24.206889622398023]]]}}))

(defn index-geodetic-with-hole
  [conn]
  (es/index-document
   conn
   idx-name
   {:name "geodetic square over South America with a hole"
    :geodetic {:type "Polygon"
               :coordinates
               [[[-81.73828125
                  -34.59704151614416]
                 [-40.78125
                  -34.59704151614416]
                 [-40.78125
                  -3.074695072369682]
                 [-81.73828125
                  -3.074695072369682]
                 [-81.73828125
                  -34.59704151614416]]
                [[-71.015625
                  -24.046463999666567]
                 [-53.525390625
                  -24.046463999666567]
                 [-53.525390625
                  -13.410994034321702]
                 [-71.015625
                  -13.410994034321702]
                 [-71.015625
                  -24.046463999666567]]]}}))

(def geodetic-intersect-coords
  [[[-112.8515625
     0]
    [-91.0546875
     0]
    [-91.0546875
     60.413852350464914]
    [-112.8515625
     60.413852350464914]
    [-112.8515625
     0]]])

(defn index-cartesian
  [conn]
  (es/index-document
   conn 
   idx-name
   {:name "cartesian square over northern Europe"
    :cartesian {:type "Polygon"
                :coordinates
                [[[-14.765625
                   34.88593094075317]
                  [40.42968749999999
                   34.88593094075317]
                  [40.42968749999999
                   58.26328705248601]
                  [-14.765625
                   58.26328705248601]
                  [-14.765625
                   34.88593094075317]]]}}))


(defn index-cartesian-with-hole
  [conn]
  (es/index-document
   conn
   idx-name
   {:name "cartesian square over Africa with hole"
    :cartesian {:type "Polygon"
                :coordinates
                [[[-8.7890625
                   -22.268764039073968]
                  [46.7578125
                   -22.268764039073968]
                  [46.7578125
                   28.613459424004414]
                  [-8.7890625
                   28.613459424004414]
                  [-8.7890625
                   -22.268764039073968]]
                 [[-3.69140625
                   -0.3515602939922709]
                  [42.1875
                   -0.3515602939922709]
                  [42.1875
                   12.554563528593656]
                  [-3.69140625
                   12.554563528593656]
                  [-3.69140625
                   -0.3515602939922709]]]}}))

(def cartesian-intersect-coords
  [[[2.109375
     13.923403897723347]
    [25.3125
     13.923403897723347]
    [25.3125
     71.85622888185527]
    [2.109375
     71.85622888185527]
    [2.109375
     13.923403897723347]]])
