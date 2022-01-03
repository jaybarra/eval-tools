(ns eval.hello.interface-test
  (:require [clojure.test :as test :refer :all]
            [eval.hello.interface :as hello]))

(deftest greet--happy-path
  (testing "Given a user name"
    (let [name "Bear"]
      (testing "When I greet the user"
        (is (= "Hello, Bear!" 
               (hello/greet name))
            "Then the user is propertly greeted.")))))
