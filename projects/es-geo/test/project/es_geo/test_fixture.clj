(ns project.es-geo.test-fixture
  (:require
   [clj-http.client :as http]
   [eval.test-helpers.interface :as test-helpers]
   [taoensso.timbre :as log])
  (:import
   [java.net ConnectException]
   [org.apache.http NoHttpResponseException]))

(defn wait-for-docker
  []
  (letfn [(check-es
           []
           (try
             (http/get "http://localhost:9200/_cat/health" {:throw-exceptions? false})
             (catch NoHttpResponseException _
               (log/info "No response from Elasticsearch"))
             (catch ConnectException _
               (log/info "No response from Elasticsearch"))))]
    (let [interval 5000
          attempts 12]
      (loop [attempts attempts
             resp (check-es)]
        (if (or (= 200 (:status resp))
                (not (pos? attempts)))
          (if (= 200 (:status resp))
            (log/info "Elasticsearch ready" )
            (throw (ex-info "Elasticsearch did not start in alotted time"
                            {:es-response (or resp "No response from Elasticsearch was returned")})))
          (do (Thread/sleep interval)
              (recur (dec attempts)
                     (check-es))))))))

(defn setup [project-name]
  (println (str "--- test setup for " project-name " ---"))
  (test-helpers/docker-compose-up
   "./projects/es-geo/resources"
   {:wait-fn wait-for-docker}))

(defn teardown [project-name]
  (println (str "--- test teardown for " project-name " ---"))
  (test-helpers/docker-compose-down "./projects/es-geo/resources"))
