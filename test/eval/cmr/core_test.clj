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
                      (-invoke [_client _command]
                        {:status 200})
                      
                      (-echo-token [_]
                        "test-token"))]
    (is (= {:status 200}
           (cmr/invoke test-client {:foo :bar})))))
