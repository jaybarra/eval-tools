(ns eval.handler-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.handler :as handler]
   [jsonista.core :as json]
   [ring.mock.request :as mock]))

(deftest api-route-test
  (let [app (handler/create-app nil)]
    (is (= {:status 200
            :headers {"Content-Type" "application/json; charset=utf-8"
                      "Access-Control-Allow-Origin" "*"}
            :body {:status "green"
                   :name "Eval Tools"
                   :version "0.1.0-SNAPSHOT"}}
           (-> (mock/request :get "/api/health")
               app
               (update :body json/read-value json/keyword-keys-object-mapper))))))
