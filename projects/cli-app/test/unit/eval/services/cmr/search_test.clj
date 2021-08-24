(ns eval.services.cmr.search-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.search :as search]
   [jsonista.core :as json]))

(deftest search-test
  (testing "search against the correct endpoint"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ query]
                     (is (= "/search/collections" (:url query)))
                     (is (= "test-echo-token" (get-in query [:headers :authorization]))))
                   (-token [_] "test-echo-token"))]
      (search/search client  :collection {:provider "FOO-PROV"})))

  (testing "options are passed to the client properly"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ query]
                     (is (= "/search/collections.umm_json" (:url query)))
                     (is (nil? (get-in query [:headers :authorization]))))
                   (-token [_] "test-echo-token"))]
      (search/search
       client
       :collection
       {:provider "FOO-PROV"}
       {:format :umm-json
        :anonymous? true}))))

(deftest query-hits-test
  (testing "checks the CMR-Hits header value"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ _]
                     {:status 200
                      :headers {:CMR-Hits "187"}})
                   (-token [_] "test-echo-token"))]
      (is (= 187 (search/query-hits client :collection {:provider "FOO-PROV"}))))))

(deftest scroll-test
  (testing "without an existing scroll-id"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ _]
                     {:status 200
                      :headers
                      {:CMR-Hits "0"
                       :CMR-Scroll-Id "foo-scroll"
                       :content-type "application/vnd.nasa.cmr.umm+json"}
                      :body (json/write-value-as-string {:feed {:items []}})})
                   (-token [_] "test-echo-token"))]
      (is (some? (search/scroll! client :collection {:provider "FOO-PROV"})))))

  (testing "existing scroll-id is used in header"
    (let [client
          (reify cmr/CmrClient
            (-invoke [_ query]
              (is (= "existing-foo-scroll" (get-in query [:headers :CMR-Scroll-Id])))
              {:status 200
               :headers
               {:CMR-Hits "0"
                :CMR-Scroll-Id "existing-foo-scroll"
                :content-type "application/vnd.nasa.cmr.umm+json"}
               :body (json/write-value-as-string {:feed {:items []}})})
            (-token [_] "test-token"))]
      (is (some? (search/scroll!
                  client
                  :collection
                  {:provider "FOO-PROV"}
                  {:CMR-Scroll-Id "existing-foo-scroll"}))))))

(deftest clear-scroll-session-test
  (let [client
        (reify cmr/CmrClient
          (-invoke [_client query]
            (is (= :post (get-in query [:method])))
            (is (= {:scroll_id "clear-foo-scroll"}
                   (json/read-value (:body query) json/keyword-keys-object-mapper)))
            (is (= "/search/clear-scroll" (get-in query [:url])))
            {:status 200})
          (-token [_] "test-token"))]
    (is (some? (search/clear-scroll-session! client "clear-foo-scroll")))))
