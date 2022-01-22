(ns eval.cmr-dash.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr-dash.core :as core :refer [app]]))

(deftest app--route-handling
  (testing "Given a request to /api"
    (let [request {:request-method :get
                   :uri "/api"}]
      (testing "When I submit the request"
        (is (= {:status 200}
               (select-keys (app request)
                            [:status]))
            "Then the status is 200"))))

  (testing "Trailing slash redirects"
    (testing "Given a request to /api/"
      (let [request {:request-method :get
                     :uri "/api/"}]
        (testing "When I submit the request"
          (is (= {:status 301
                  :headers {"Location" "/api"}}
                 (select-keys (app request)
                              [:status
                               :headers]))
              "Then I get a redirect to /api")))))

  (testing "404 handling"
    (testing "Given a request to a non-existent route"
      (let [request {:request-method :get
                     :uri "/a-route-that-doesnt-exist"}]
        (testing "When I submit the request"
          (is (= {:status 404}
                 (select-keys (app request) [:status]))
              "Then I get a 404"))))))

(deftest index--valid--200
  (let [request {:request-method :get
                 :uri "/index.html"}]
    (is (= {:status 200}
           (select-keys (app request) [:status])))))


(deftest index--redirect--302
  (let [request {:request-method :get
                 :uri "/"}]
    (is (= {:status 302}
           (select-keys (app request) [:status])))))
