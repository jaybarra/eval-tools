(ns eval.cmr.bulk.granule-test
  (:require
   [clojure.test :refer :all]
   [eval.cmr.bulk.granule :as bulk-granule]
   [eval.cmr.core :as cmr]
   [reitit.ring :as ring]
   [ring.adapter.jetty :as jetty]
   [ring.util.response :refer [response status]]))
