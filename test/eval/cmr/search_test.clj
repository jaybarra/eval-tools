(ns eval.cmr.search-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.search :as search]
   [jsonista.core :as json]))

(deftest search-test
  (testing "correct url for concept-type"
    (are [concept-type url]
        (= url (:url (search/search concept-type {:provider "FOO"})))
      :collection "/search/collections"
      :granule "/search/granules"
      :variable "/search/variables")))

(deftest scroll-test
  (testing "scroll-id header is set"
    (is (= "1234"
           (get-in (search/scroll :collection
                                  {:provider "FOO"}
                                  "1234")
                   [:headers "CMR-Scroll-Id"])))))

(deftest clear-scroll-session-test
  (let [{:keys [body] :as req} (search/clear-scroll-session "12345")]
    (is (= {:method :post
            :url "/search/clear-scroll"
            :headers {:content-type "application/json"}}
           (dissoc req :body)))
    (is (= {"scroll_id" "12345"}
           (json/read-value body)))))
