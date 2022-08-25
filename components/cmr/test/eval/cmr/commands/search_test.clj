(ns eval.cmr.commands.search-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.client :as cmr]
   [eval.cmr.commands.search :as search]))

(deftest search-test
  (is (spec/valid? ::cmr/command (search/search :collection {:provider "BAR"})))

  (testing "correct url for concept-type"
    (are [concept-type url]
         (= url (get-in (search/search concept-type {:provider "FOO"}) [::cmr/request :url]))
      :collection "/search/collections"
      :collections "/search/collections"
      :granule "/search/granules"
      :granules "/search/granules"
      :variable "/search/variables")))

(deftest search-after-test
  (is (spec/valid? ::cmr/command (search/search-after :collection {} "[\"sa-key\", \"123\", \"456\"]"))))

(deftest search-post
  (testing "search using POST and multi-part"
    (is (spec/valid? ::cmr/command (search/search-post
                                    :collection
                                    [{:name "shapefile"
                                      :content (clojure.java.io/file "/tmp/some_file")
                                      :mime-type "text/plain"}])))))

(deftest search-after-post
  (is (spec/valid? ::cmr/command (search/search-after-post
                                  :collection
                                  [{:name "shapefile"
                                    :content (clojure.java.io/file "/tmp/some_file")
                                    :mime-type "text/plain"}]
                                  "[\"a\", \"b\", \"c\"]"))))
