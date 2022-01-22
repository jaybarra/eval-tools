(ns eval.cmr-cli.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr-cli.core :as app]))

(deftest config-test
  (testing "When I get the app config"
    (is (map? (app/config))
        "The value is a map")))
