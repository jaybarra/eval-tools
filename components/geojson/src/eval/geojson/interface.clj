(ns eval.geojson.interface
  (:require
   [eval.geojson.core :as core]))

(defn validate
  [geojson]
  (core/validate geojson))

(defn split-polygon
  "Splits a polygon that crosses the anti-meridian into compatible adjoining polygons."
  [polygon]
  (core/split-polygon polygon))
