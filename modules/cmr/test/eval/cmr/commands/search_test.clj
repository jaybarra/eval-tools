(ns eval.cmr.commands.search-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.commands.search :as search]
   [eval.cmr.core :as cmr]))

(deftest search-test
  (is (spec/valid? ::cmr/command (search/search :collection {:provider "BAR"})))

  (testing "correct url for concept-type"
    (are [concept-type url]
        (= url (get-in (search/search concept-type {:provider "FOO"}) [:request :url]))
      :collection "/search/collections"
      :collections "/search/collections"
      :granule "/search/granules"
      :granules "/search/granules"
      :variable "/search/variables")))

(deftest scroll-test
  (is (spec/valid? ::cmr/command (search/scroll :collection 
                                                {:provider "foo"}  
                                                "scroll-key")))
  (testing "scroll-id header is set"
    (is (= "1234"
           (get-in (search/scroll :collection
                                  {:provider "FOO"}
                                  "1234")
                   [:request :headers "CMR-Scroll-Id"])))))

(deftest clear-scroll-session-test
  (let [command (search/clear-scroll-session "12345")]
    (is (spec/valid? ::cmr/command command))
    (is (= {:request {:method :post
                      :url "/search/clear-scroll"
                      :headers {:content-type "application/json"}
                      :body {:scroll_id "12345"}}
            ::cmr/category :search}
           command)))

  (let [command (search/clear-scroll-session 56789)]
    (is (= {:request {:method :post
                      :url "/search/clear-scroll"
                      :headers {:content-type "application/json"}
                      :body {:scroll_id "56789"}}
            ::cmr/category :search}
           command))))

(deftest search-after-test
  (is (spec/valid? ::cmr/command (search/search-after :collection {} "[sa-key, 123, 456]"))))
