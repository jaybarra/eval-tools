(ns eval.cmr.core-test
  (:require
   [clojure.test :refer :all]
   [eval.cmr.core :as cmr]))

(deftest cmr-conn-test
  (are [env out] (= out (cmr/cmr-conn env))
    :prod {::cmr/env :prod
           ::cmr/url "https://cmr.earthdata.nasa.gov"}

    :uat {::cmr/env :uat
          ::cmr/url "https://cmr.uat.earthdata.nasa.gov"}

    :sit {::cmr/env :sit
          ::cmr/url "https://cmr.sit.earthdata.nasa.gov"}))


