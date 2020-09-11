(ns eval.cmr.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.spec.alpha :as spec]
   [eval.cmr.core :as cmr :refer [state->cmr]]))

(def state {:connections
            {::cmr/cmr {::cmr/env :local
                        ::cmr/url "http://localhost:3000"}}})

(deftest state->cmr-opts-test
  (let [result (state->cmr state)]
    (is (spec/valid? ::cmr/cmr result)
        (spec/explain-data ::cmr/cmr result))))

