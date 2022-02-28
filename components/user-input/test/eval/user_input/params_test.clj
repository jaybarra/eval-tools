(ns eval.user-input.params-test
  (:require
   [clojure.test :refer [deftest are]]
   [eval.user-input.params :as params]))

(deftest named?
  (are [arg is-named?] (= is-named? (params/named? arg))
    "p:LPDAAC" true
    "collections" false
    nil false))
