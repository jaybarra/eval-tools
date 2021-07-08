(ns eval.cmr.core-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [eval.cmr.core :as cmr]
   [jsonista.core :as json]))

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
                      
                      (-echo-token [_]
                        "test-token"))]
    (is (= {:status 200}
           (cmr/invoke test-client {:request {:method :get
                                              :url "/a/test"}})))
    (testing "it rejects invalid commands"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Invalid CMR command"
                            (cmr/invoke test-client {:bad :command}))))))

(defrecord ^:private MockClient [id url opts]
  cmr/CmrClient

  (-invoke [this query]
    (is (= "internal://localhost:32303/collections.umm_json"
           (:url query)))
    {:status 200})
  (-echo-token [this]
    "foo"))

(deftest endpoints-override-test
  (let [client (->MockClient :foo
                             "http://test"
                             {:endpoints {:search "internal://localhost:32303"}})]
    (is (= {:status 200}
           (cmr/invoke client
                       {:request
                        {:method :get
                         :url "/search/collections.umm_json"}})))))
