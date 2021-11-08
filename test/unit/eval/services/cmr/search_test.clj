(ns eval.services.cmr.search-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.search :as search]
   [jsonista.core :as json]))

(deftest search-test
  (testing "search against the correct endpoin"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ command]
                     (is (= "/search/collections" (get-in command [:request :url])))
                     (is (= "test-echo-token" (get-in command [:request :headers :authorization]))))
                   (-token [_] "test-echo-token"))]
      (search/search client :collection {:provider "FOO-PROV"})))

  (testing "options are passed to the client properly"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ command]
                     (is (= "/search/collections.umm_json" (get-in command [:request :url])))
                     (is (nil? (get-in command [:request :headers :authorization]))))
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
            (-invoke [_ command]
              (is (= "existing-foo-scroll" (get-in command [:request :headers :CMR-Scroll-Id])))
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
          (-invoke [_client command]
            (is (= :post (get-in command [:request :method])))
            (is (= {:scroll_id "clear-foo-scroll"}
                   (json/read-value (get-in command [:request :body]) json/keyword-keys-object-mapper)))
            (is (= "/search/clear-scroll" (get-in command [:request :url])))
            {:status 200})
          (-token [_] "test-token"))]
    (is (some? (search/clear-scroll-session! client "clear-foo-scroll")))))
