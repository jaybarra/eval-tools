(ns health-check-steps
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]
   [clojure.test :refer [is]]
   [eval.system :as system]
   [lambdaisland.cucumber.dsl :refer [Given When Then]]))

(Given "The server is running" [state]
  (assoc state :system (system/-main)))

(When "I send a {string} request to {string}" [state method url]
  ;; Write code here that turns the phrase above into concrete actions
  (assoc state :response (client/request
                          {:method (keyword (str/lower-case method))
                           :url (str "http://localhost:8880/" url)})))

(Then "I get a response with {string}" [state content-type]
  ;; Write code here that turns the phrase above into concrete actions
  (is (str/starts-with?
       (get-in state [:response :headers "Content-Type"])
       content-type))
  state)
