(ns eval.cmr.cli.main-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.cli.main :as app]))

(deftest config-test
  (testing "When I get the app config"
    (is (map? (app/config))
        "The value is a map")))
