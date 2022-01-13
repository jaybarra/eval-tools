(ns eval.test-helpers.interface
  (:require
   [eval.test-helpers.core :as core]))

(defn within?
  [^Double delta a b]
  (core/within? delta a b))
