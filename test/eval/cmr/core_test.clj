(ns eval.cmr.core-test
  (:require
   [clojure.test :refer :all]
   [eval.cmr.core :as cmr]))

(deftest client-test
  (testing "not found in config"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"No entry found in configuration for specified CMR instance"
         (cmr/client :foo)))))


