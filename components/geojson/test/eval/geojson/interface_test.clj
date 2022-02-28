(ns eval.geojson.interface-test
  (:require [clojure.test :refer [deftest testing is]]
            [eval.geojson.interface :as geojson]))

(deftest validate--valid-geojson--yields-input
  (let [input {:geometry
               {:coordinates
                [[[-129.375 24.206889622398023]
                  [-68.5546875
                   24.206889622398023]
                  [-68.5546875
                   50.736455137010665]
                  [-129.375 50.736455137010665]
                  [-129.375
                   24.206889622398023]]]
                :type "Polygon"}
               :properties
               {:name "north america polygon"}
               :type "Feature"}]
    (is (= input (geojson/validate input)))))

(deftest convert--pole--geometry-collection
  (testing "Polar projection converts to epsg 4536"
    (let [original {:type :polygon
                    :coordinates [[[45 85]
                                   [135 85]
                                   [-135 85]
                                   [-45 85]
                                   [45 85]]]}]
      (is (= {:type :geometrycollection
              :geometries [{:type :polygon
                            :coordinates [[[-180 85]
                                           [0 85]
                                           [0 90]
                                           [-180 90]
                                           [-180 85]]]}
                           {:type :polygon
                            :coordaintes [[[0 85]
                                           [180 85]
                                           [180 90]
                                           [0 90]
                                           [0 85]]]}]}
             (geojson/split-polygon original))))))
