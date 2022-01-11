(ns eval.es-driver.gdelt-test
  (:require [clojure.test :refer [deftest testing is]]
            [eval.es-driver.gdelt :as gdelt]))

(deftest index-mapping
  (is (map? gdelt/gdelt-index-mapping)))
