(ns eval.cmr.search-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.search :as search]
   [jsonista.core :as json]))

(deftest search-test
  (testing "correct url for concept-type"
    (are [concept-type url]
        (= url (get-in (search/search concept-type {:provider "FOO"}) [:request :url]))
      :collection "/search/collections"
      :collections "/search/collections"
      :granule "/search/granules"
      :granules "/search/granules"
      :variable "/search/variables")))

(deftest scroll-test
  (testing "scroll-id header is set"
    (is (= "1234"
           (get-in (search/scroll :collection
                                  {:provider "FOO"}
                                  "1234")
                   [:request :headers "CMR-Scroll-Id"])))))

(deftest clear-scroll-session-test
  (let [command (search/clear-scroll-session "12345")]
    (is (= {:request {:method :post
                      :url "/search/clear-scroll"
                      :headers {:content-type "application/json"}}}
           (update command :request dissoc :body)))
    (is (= {"scroll_id" "12345"}
           (json/read-value (get-in command [:request :body])))))

  (let [command (search/clear-scroll-session 56789)]
    (is (= {:request {:method :post
                      :url "/search/clear-scroll"
                      :headers {:content-type "application/json"}}}
           (update command :request dissoc :body)))
    (is (= {"scroll_id" "56789"}
           (json/read-value (get-in command [:request :body]))))))
