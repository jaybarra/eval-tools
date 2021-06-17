(ns eval.services.cmr.search-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.test-core :as cmr-test]
   [eval.services.cmr.search :as search]
   [jsonista.core :as json]))

(deftest search-test
  (testing "search against the correct endpoint"
    (let [test-client (cmr-test/client
                       (fn [_ query]
                         (is (= "/search/collections" (get query :url))
                             {:status 200}))
                       (constantly "test-echo-token"))
          context (cmr-test/context :test test-client)]
      (search/search context :test :collection {:provider "FOO-PROV"}))))

(deftest query-hits-test
  (testing "checks the CMR-Hits header value"
    (let [test-client (cmr-test/client
                       (fn [_ query]
                         {:status 200
                          :headers {:CMR-Hits "187"}})
                       (constantly "test-echo-token"))
          context (cmr-test/context :test test-client)]
      (is (= 187
             (search/query-hits context :test :collection {:provider "FOO-PROV"}))))))

(deftest scroll-test
  (testing "without an existing scroll-id"
    (let [test-client (cmr-test/client
                       (constantly {:status 200
                                    :headers
                                    {:CMR-Hits "0"
                                     :CMR-Scroll-Id "foo-scroll"
                                     :content-type "application/vnd.nasa.cmr.umm+json"}
                                    :body (json/write-value-as-string {:feed {:items []}})})
                       (constantly "test-echo-token"))
          context (cmr-test/context :test test-client)]
      (is (some? (search/scroll! context :test :collection {:provider "FOO-PROV"})))))

  (testing "existing scroll-id is used in header"
    (let [test-client
          (reify cmr/CmrClient
            (-invoke [_client query]
              (is (= "existing-foo-scroll" (get-in query [:headers :CMR-Scroll-Id])))
              {:status 200
               :headers {:CMR-Hits "0"
                         :CMR-Scroll-Id "existing-foo-scroll"
                         :content-type "application/vnd.nasa.cmr.umm+json"}
               :body (json/write-value-as-string {:feed {:items []}})})

            (-echo-token [_]
              "test-token"))
          context (cmr-test/context :test test-client)]
      (is (some? (search/scroll!
                  context
                  :test
                  :collection
                  {:provider "FOO-PROV"}
                  {:CMR-Scroll-Id "existing-foo-scroll"}))))))

(deftest clear-scroll-session-test
  (let [test-client
        (reify cmr/CmrClient
          (-invoke [_client query]
            (is (= :post (get-in query [:method])))
            (is (= {:scroll_id "clear-foo-scroll"}
                   (json/read-value (:body query) json/keyword-keys-object-mapper)))
            (is (= "/search/clear-scroll" (get-in query [:url])))

            {:status 200})

          (-echo-token [_]
            "test-token"))
        context (cmr-test/context :test test-client)]

    (is (some? (search/clear-scroll-session!
                context
                :test
                "clear-foo-scroll")))))
