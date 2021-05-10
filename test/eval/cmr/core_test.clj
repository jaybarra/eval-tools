(ns eval.cmr.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.spec.alpha :as spec]
   [environ.core :as env]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as muuntaja]
   [reitit.coercion.spec]
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja-mw]
   [ring.adapter.jetty :as jetty]
   [ring.util.response :refer [response status]]))

(def cmr-conn (cmr/cmr-conn :local))

(deftest cmr-conn-test
  (are [env out] (= out (cmr/cmr-conn env))
    :prod {::cmr/env :prod
           ::cmr/url "https://cmr.earthdata.nasa.gov"}

    :uat {::cmr/env :uat
          ::cmr/url "https://cmr.uat.earthdata.nasa.gov"}

    :sit {::cmr/env :sit
          ::cmr/url "https://cmr.sit.earthdata.nasa.gov"}
    
    :wl {::cmr/env :wl
         ::cmr/url "http://localhost:9999"}))


