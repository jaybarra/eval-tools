(ns eval.cmr.legacy.tokens-test
  "Actions for interacting with the CMR legacy systems."
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest is]]
   [clojure.xml :as xml]
   [clojure.zip :as zip]
   [eval.cmr.legacy.tokens :as tokens]
   [eval.cmr.core :as cmr])
  (:import
   [java.io ByteArrayInputStream]))

(deftest get-token-test
  (let [{{:keys [^String body]} :request :as command} (tokens/get-token {:username "foo" :password "bar"})]
    (is (spec/valid? ::cmr/command command))
    (is (= {:request
            {:method :post
             :url "/legacy-services/rest/tokens"
             :headers {"Content-Type" "application/xml"}}}
           (update-in command [:request] dissoc :body)))
    (is (= (xml/parse (ByteArrayInputStream.
                       (.getBytes "<token>
                                     <username>foo</username>
                                     <password>bar</password>
                                     <client_id>test-client</client_id>
                                     <user_ip_address>127.0.0.1</user_ip_address>
                                   </token>")))
           (xml/parse (ByteArrayInputStream. (.getBytes body)))))))

(deftest get-token-info-test
  (let [{{:keys [body]} :request :as command} (tokens/get-token-info "foo")]
    (is (spec/valid? ::cmr/command command))
    (is (= {:request
            {:method :post
             :url "/legacy-services/rest/tokens/get_token_info"
             :headers {"Content-Type" "application/json"}}}
           (update-in command [:request] dissoc :body)))
    (is (= {:id "foo"} body))))

(deftest echo-token-soap-message-test
  (let [msg (tokens/echo-token-soap-message
             "username" "secret" "client")]
    (is (string? msg))
    (is (not (nil? (-> (ByteArrayInputStream. (.getBytes msg))
                       xml/parse
                       zip/xml-zip))))))
