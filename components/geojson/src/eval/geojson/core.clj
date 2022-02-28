(ns eval.geojson.core
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.java.io :as io]
   [clojure.math.numeric-tower :as math]
   [jsonista.core :as json])
  (:import
   [org.geotools.geojson.geom GeometryJSON]
   ;; JTS (Java Topology Suite) Topology Suite
   org.geotools.geometry.jts.JTS
   ;; Coordinate Reference System
   org.geotools.referencing.CRS))

(defmulti validate-geojson
  (fn [geojson]
    (csk/->kebab-case-keyword (:type geojson))))

(defmethod validate-geojson :feature
  [geojson])

(comment
  (def input (.read (GeometryJSON.)
                    (json/write-value-as-string
                     {:type "Polygon"
                      :coordinates [[[45 85]
                                     [135 85]
                                     [-135 85]
                                     [-45 85]
                                     [45 85]]]})))
  (def arctic-polar-sterograph (CRS/decode "EPSG:3995"))
  (def wgs (CRS/decode "EPSG:4326"))

  (def polar->wgs (CRS/findMathTransform wgs arctic-polar-sterograph))

  (JTS/transform input polar->wgs)
  )

(defn crosses-am?
  "Takes a pair of longitudes and calculates line between the longitudes will cross the anti-meridian.
  If a value is returned it will indicate the direction of the crossing, otherwise nil.
  
  It is assumed the direction is from the first to the second longitude."
  [lon1 lon2 & [delta_threshold]]
  (when (some #(> (math/abs %) 180.0) [lon1 lon2])
    (throw (ex-info "Longitudes must be in degrees between [-180.0 180.0]" {:longitudes [lon1 lon2]})))
  (let [delta_threshold (or delta_threshold 180.0)]
    (when (> (math/abs (- lon2 lon1)) delta_threshold)
      (if (pos? (- lon1 lon2))
        :west-east
        :east-west))))

(defn validate
  [geojson]
  (validate-geojson geojson))

(defn- am-crossings
  "Returns the anti-meridian crossings in a ring of a polygon.
  Takes rings in the form of [[lon1 lat1] [lon2 lat2]...]"
  [ring]
  (remove nil? (map crosses-am?
                    (map first ring)
                    (map first (rest ring)))))

(defn split-polygon
  [polygon]
  (let [outer-ring (first polygon)
        crossings (am-crossings outer-ring)]
    crossings))

(comment
  (split-polygon [[[45 85]
                   [135 85]
                   [-135 85]
                   [-45 85]
                   [45 85]]])
  )
