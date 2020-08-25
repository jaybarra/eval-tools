(ns maths.cmr-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as spec]
            [maths.cmr :as cmr :refer [state->cmr-opts]]))

(def state {:connections
            {::cmr/cmr {::cmr/env :local
                        ::cmr/url "http://localhost:3000"}}})

(deftest state->cmr-opts-test
  (let [result (state->cmr-opts state)]
    (is (spec/valid? ::cmr/cmr result)
        (spec/explain-data ::cmr/cmr result))))

