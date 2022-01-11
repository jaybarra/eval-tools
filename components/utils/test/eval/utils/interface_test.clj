(ns eval.utils.interface-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.utils.interface :as utils]))

(deftest edn->json--happy-path
  (testing "Given an EDN map"
    (let [edn {:foo "bar"}]
      (testing "When I convert the EDN to JSON"
        (is (= "{\"foo\":\"bar\"}" (utils/edn->json edn))
            "Then the EDN is converted to JSON correctly")))))
