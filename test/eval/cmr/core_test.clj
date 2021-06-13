(ns eval.cmr.core-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.core :as cmr]
   [jsonista.core :as json]))

(deftest client-test
  (testing "not found in config"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"No entry"
         (cmr/create-client :foo))))

  (testing "using a map"
    (is (some?
         (cmr/create-client {::cmr/id :foo
                             ::cmr/url "http://localhost"}))))

  (testing "using an invalid map throws an exception"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Invalid"
         (cmr/create-client {:bad :entry})))))

(deftest invoke-test
  (let [test-client (reify
                      cmr/CmrClient
                      (-invoke [_client _command]
                        {:status 200})
                      
                      (-echo-token [_]
                        "test-token"))]
    (is (= {:status 200}
           (cmr/invoke test-client {:foo :bar})))))

(deftest search-test
  (testing "search against the correct endpoint"
    (let [test-client (reify cmr/CmrClient
                        (-invoke [_client query]
                          (is (= "/search/collections" (get-in query [:url])))
                          {:status 200})
                        
                        (-echo-token [_]
                          "test-token"))]
      (is (= {:status 200}
             (cmr/search test-client :collection {:provider "FOO-PROV"}))))))

(deftest query-hits-test
  (testing "checks the CMR-Hits header value"
    (let [test-client  
          (reify cmr/CmrClient
            (-invoke [_client query]
              (is (= "/search/collections" (get-in query [:url])))
              {:status 200
               :headers {:CMR-Hits "187"}})
            
            (-echo-token [_]
              "test-token"))]
      (is (= 187
             (cmr/query-hits test-client :collection {:provider "FOO-PROV"}))))))

(deftest scroll-test
  (testing "without an existing scroll-id"
    (let [test-client
          (reify cmr/CmrClient
            (-invoke [_client query]
              {:status 200
               :headers {:CMR-Hits "0"
                         :CMR-Scroll-Id "foo-scroll"
                         :content-type "application/vnd.nasa.cmr.umm+json"}
               :body (json/write-value-as-string {:feed {:items []}})})
            
            (-echo-token [_]
              "test-token"))]
      (is (some? (cmr/scroll! test-client :collection {:provider "FOO-PROV"})))))

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
              "test-token"))]
      (is (some? (cmr/scroll!
                  test-client
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
            "test-token"))]
    
    (is (some? (cmr/clear-scroll-session!
                test-client
                "clear-foo-scroll")))))
