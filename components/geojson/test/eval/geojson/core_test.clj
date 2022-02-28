(ns eval.geojson.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.geojson.core :as geojson]))

(deftest crosses-am--crossings-return-direction
  (is (= :east-west (geojson/crosses-am? -179 179)))
  (is (= :west-east (geojson/crosses-am? 179 -179)))
  (is (= :west-east (geojson/crosses-am? 179 178)))
  (is (= :west-east (geojson/crosses-am? -178 -179))))

(deftest crosses-am--no-crossing--returns-nil
  (is (nil? (geojson/crosses-am? -1 1)))
  (is (nil? (geojson/crosses-am? -178 -179)))
  (is (nil? (geojson/crosses-am? 179 178))))
