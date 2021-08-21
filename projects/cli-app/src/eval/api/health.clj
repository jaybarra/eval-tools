(ns eval.api.health
  "Health endpoint

  Will return a 200 status message if the system is functioning correctly

  Statuses:
  :green  - normal operation
  :yellow - required compoenents are degraded
  :red    - required components are not available or degraded"
  (:require
   [ring.util.response :refer [response]]))

(def health-check (constantly (response
                               {:name "Eval Tools"
                                :version "0.1.0-SNAPSHOT"
                                :status :green})))

(def routes
  ["/health" {:handler health-check}])
