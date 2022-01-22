(ns eval.user-input.interface-test
  (:require [clojure.test :refer [deftest testing is]]
            [eval.user-input.interface :as user-input]))

(deftest parse-args-test
  (testing "Given some user input"
    (let [input ["search" "sit" "collections" "p:PODAAC"]]
      (is (= {:cmd :search
              :cmr :sit
              :args ["collections" "p:PODAAC"]}
             (user-input/parse-args input))))))
