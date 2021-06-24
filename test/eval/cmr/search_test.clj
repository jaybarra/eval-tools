(ns eval.cmr.search-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.search :as search]))

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
                   [:headers :CMR-Scroll-Id])))))
