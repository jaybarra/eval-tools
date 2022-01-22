(ns eval.test-helpers.interface
  (:require
   [eval.test-helpers.core :as core]))

(defn within?
  [^Double delta a b]
  (core/within? delta a b))

(defn docker-compose-up
  [dir opts]
  (core/docker-compose-up dir opts))

(defn docker-compose-down
  [dir]
  (core/docker-compose-down dir))
