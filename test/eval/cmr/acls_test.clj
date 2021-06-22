(ns eval.cmr.acls-test
  (:require
   [clojure.test :refer :all]
   [eval.cmr.acls :as acls]))

(deftest acls-test
  (is (= {:url "/access-control/acls"
          :method :get}
         (acls/get-acls))))
