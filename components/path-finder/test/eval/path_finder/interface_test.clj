(ns eval.path-finder.interface-test
  (:require
   [clojure.test :as test :refer :all]
   [eval.path-finder.interface :as path-finder]))

(deftest generate-path--case
  (is (= ["a" "b"]
         (path-finder/generate-path [{:id "a"
                                      :position [1 1]}
                                     {:id "b"
                                      :position [0 0]}]))))
