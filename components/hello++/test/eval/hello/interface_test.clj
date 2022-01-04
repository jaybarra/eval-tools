(ns eval.hello.interface-test
  (:require [clojure.test :refer [deftest testing is]]
            [eval.hello.interface :as hello]))

(deftest greet--improved-happy-path
  (testing "Given a user name"
    (let [name "Bear"]
      (testing "When I greet the user"
        (is (= "Hello++, Bear!"
               (hello/greet name))
            "Then the user is propertly greeted better than the first version of hello.")))))
