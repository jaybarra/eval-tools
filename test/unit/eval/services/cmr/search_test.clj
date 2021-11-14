(ns eval.services.cmr.search-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.search :as search]))

(deftest search-test
  (testing "search against the correct endpoin"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ command]
                     (is (= "/search/collections" (get-in command [::cmr/request :url])))
                     (is (= "test-echo-token" (get-in command [::cmr/request :headers :authorization]))))
                   (-token [_] "test-echo-token"))]
      (search/search client :collection {:provider "FOO-PROV"})))

  (testing "options are passed to the client properly"
    (let [client (reify cmr/CmrClient
                   (-invoke [_ command]
                     (is (= "/search/collections.umm_json" (get-in command [::cmr/request :url])))
                     (is (nil? (get-in command [::cmr/request :headers :authorization]))))
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
