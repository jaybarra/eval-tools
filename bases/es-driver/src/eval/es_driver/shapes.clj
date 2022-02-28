(ns eval.es-driver.shapes
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [eval.elastic.interface :as es]
   [jsonista.core :as json])
  (:import
   java.io.File))

(defn- load-geojson
  [dir]
  (let [files (-> dir
                  io/resource
                  .getPath
                  File.
                  .listFiles)]
    (for [f files
          :when (.isFile f)]
      (-> f slurp (json/read-value json/keyword-keys-object-mapper)))))

(defn- load-wkt
  [dir]
  (let [files (-> dir
                  io/resource
                  .getPath
                  File.
                  .listFiles)]
    (for [f files
          :when (.isFile f)]
      (str/trim-newline (-> f slurp)))))

(def shapes (load-geojson "geojson"))
(def wkt-shapes (load-wkt "wkt"))

(def idx-name "shape-searchables")

(defn create-index
  "Creates an index `shape-searchables` with `geo_shape` and `shape` fields."
  [conn]
  (let [idx idx-name
        config {:mappings
                {:properties
                 {:cartesian {:type "shape"}
                  :geodetic {:type "geo_shape"}}}}]
    (es/delete-index conn idx)
    (es/create-index conn idx config)))

(defn ingest-geojson
  "Ingest a geojson feature.
   
  TODO: feature collections are not supported yet"
  [conn features]
  (doseq [feature features]
    ;; TODO handle FeatureCollections
    (when (not= "FeatureCollection" (:type feature))
      (es/index-document conn idx-name
                         (merge (:properties feature)
                                {:geodetic (:geometry feature)
                                 :geojson true
                                 :cartesian (:geometry feature)})))))
(defn ingest-wkt
  "Ingest a Well Known Text (WKT) shape"
  [conn shapes]
  (doseq [shape shapes]
    (es/index-document conn idx-name
                       {:geodetic shape
                        :wkt true
                        :cartesian shape})))

(comment
  (do
    (es/delete-index {:url "http://localhost:9209/"} idx-name)
    (create-index {:url "http://localhost:9209/"})
    (ingest-geojson {:url "http://localhost:9209/"} shapes)
    (ingest-wkt {:url "http://localhost:9209/"} wkt-shapes))
  )
