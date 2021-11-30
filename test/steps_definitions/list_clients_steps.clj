(ns list-clients-steps
  (:require
   [lambdaisland.cucumber.dsl :refer [Given When Then pending!]]))

(Given "the input {string}" [state input]
       (pending!))

(When "the tool is run" [state]
      ;; Write code here that turns the phrase above into concrete actions
      (pending!))

(Then "I get a list of cmr clients" [state]
      ;; Write code here that turns the phrase above into concrete actions
      (pending!))
