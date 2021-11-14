(ns eval.cmr.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as m]))

(deftest client-test
  (is (some?
       (cmr/create-client {:id :foo
                           :url "http://localhost"})))

  (testing "using an invalid map throws an exception"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Invalid"
         (cmr/create-client {:bad :entry})))))

(deftest invoke-test
  (let [test-client (reify
                      cmr/CmrClient
                      (-invoke [_client _query]
                        {:status 200})
                      
                      (-token [_]
                        "test-token"))]
    (is (= {:status 200}
           (cmr/invoke test-client {::cmr/request
                                    {:method :get
                                     :url "/a/test"}
                                    ::cmr/category :test})))
    (testing "it rejects invalid commands"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Invalid CMR command"
                            (cmr/invoke test-client {:bad :command}))))))

(deftest xml-formats-handled
  (let [data (m/decode-response-body
              cmr/m
              {:status 200
               :headers {"Content-Type" "application/xml"}
               :body "<xml>value</xml>"})]
    (is (= "<xml>value</xml>" data))))

(defrecord ^:private MockClient [cfg]
  cmr/CmrClient

  (-invoke [_ command]
    (is (= "internal://localhost:32303/collections.umm_json"
           (get-in command [::cmr/request :url])))
    {:status 200})
  (-token [_] "foo"))

(deftest endpoints-override-test
  (let [client (->MockClient {:endpoints {:search "internal://localhost:32303"}})]
    (is (= {:status 200}
           (cmr/invoke client
                       {::cmr/request
                        {:method :get
                         :url "/search/collections.umm_json"}
                        ::cmr/category :mock})))))
