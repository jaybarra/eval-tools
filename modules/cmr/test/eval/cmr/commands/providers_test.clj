(ns eval.cmr.commands.providers-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.commands.providers :as providers]
   [eval.cmr.core :as cmr]))

(deftest get-providers-test
  (is (spec/valid? ::cmr/command (providers/get-providers)))
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
    (is (spec/valid? ::cmr/command command))
    (is (= {:request
            {:method :post
             :url "/metadata-db/providers"
             :headers {:content-type "application/json"}
             :body prov}
            :opts {:anonymous? false}}
           command)))

  (testing "invalid providers rejected"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Invalid provider"
                          (providers/create-provider {:id :blank})))))
