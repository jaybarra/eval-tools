(ns eval.services.cmr.providers-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.providers :as providers]))

(deftest create-provider-test
  (let [prov-def {:provider-id "SHORT_PROV"
                  :short-name "shorty"
                  :cmr-only false
                  :small false}
        client (reify cmr/CmrClient
                 (-invoke [_ command]
                   (is (= {:url "/ingest/providers"
                           :method :post
                           :headers {:content-type "application/json"
                                     :authorization "token"}
                           :body prov-def}
                          (::cmr/request command))))
                 (-token [_] "token"))]
    (providers/create-provider
     client
     prov-def)))

(deftest get-providers-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ command]
                   (is (= {:url "/ingest/providers"
                           :method :get
                           :headers {:authorization "token"}}
                          (::cmr/request command))))
                 (-token [_] "token"))]
    (providers/get-providers client)))
