(ns eval.user-input.core-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.user-input.core :as core]))

(deftest named?
  (are [arg is-named?] (= is-named? (core/named? arg))
    "p:LPDAAC" true
    "collections" false
    nil false))

(deftest extract-named-test
  (is (= ["fmt:json"]
         (core/extract-named ["a" "b" "fmt:json"]))))
