(ns eval.cmr.legacy.tokens-test
  "Actions for interacting with the CMR legacy systems."
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest testing is are]]
   [clojure.xml :as xml]
   [clj-http.client :as http]
   [eval.cmr.core :as cmr]
   [eval.cmr.legacy.tokens :as tokens]))

(set! *warn-on-reflection* true)

(deftest get-token-test
  (let [{:keys [^String body] :as req} (tokens/get-token {:username "foo" :password "bar"})]
    (is (= {:method :post
            :url "/legacy-services/rest/tokens"
            :headers {"Content-Type" "application/xml"}}
           (dissoc req :body)))
    (is (= (xml/parse (java.io.ByteArrayInputStream.
                       (.getBytes "<token>
                                     <username>foo</username>
                                     <password>bar</password>
                                     <client_id>test-client</client_id>
                                     <user_ip_address>127.0.0.1</user_ip_address>
                                   </token>")))
           (xml/parse (java.io.ByteArrayInputStream.
                       (.getBytes body)))))))

(deftest get-token-info-test
  (let [{:keys [body] :as req} (tokens/get-token-info "foo")]
    (is (= {:method :post
            :url "/legacy-services/rest/tokens/get_token_info"
            :headers {"Content-Type" "application/json"}}
           (dissoc req :body)))
    (is (= {:id "foo"} body))))

(deftest echo-token-soap-message-test
  (is (string? (tokens/echo-token-soap-message
                "username" "secret" "client"))))
