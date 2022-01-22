(ns eval.kibana.interface-test
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]
   [clojure.test :refer [deftest testing is]]
   [eval.kibana.interface :as kibana]))

(deftest submit-saved-object--when-all-valid--then-status-200
  (let [so {}]
    (with-redefs-fn {#'client/post
                     (fn [url _]
                       (is (str/ends-with? url "/api/saved_objects/_import"))
                       {:status 200})}
      #(is (= {:status 200}
              (kibana/submit-saved-object {:url "http://localhost"} so))))))
