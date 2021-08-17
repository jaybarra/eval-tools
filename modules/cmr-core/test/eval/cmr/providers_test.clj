(ns eval.cmr.providers-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.providers :as providers]
   [jsonista.core :as json]))

(deftest get-providers-test
  (is (= {:request
          {:method :get
           :url "/metadata-db/providers"}}
         (providers/get-providers))))

(deftest create-provider-test
  (let [prov {:provider-id "FOO"
              :short-name "foo provider"
              :cmr-only true
              :small false}
        command (providers/create-provider prov {:anonymous? false})]
    (is (= {:request
            {:method :post
             :url "/metadata-db/providers"
             :headers {"Content-Type" "application/json"}}
            :opts {:anonymous? false}}
           (update command :request dissoc :body)))
    (is (= prov (json/read-value (get-in command [:request :body])
                                 json/keyword-keys-object-mapper))))

  (testing "invalid providers rejected"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Invalid provider"
                          (providers/create-provider {:id :blank})))))
