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

#_(def cmr-handler
    (ring/ring-handler
     (ring/router
      ["/ingest"
       ["providers"
        ["/:provider"
         ["/bulk-update"
          ["/granules" ::granule-bulk-update-create]]]]
       ["/granule-bulk-update"
        ["/status"
         ["" ::granule-bulk-update-update]
         ["/:id" ::granule-bulk-update-job-get]]]]
      {:data {:middleware [muuntaja-mw/format-response-middleware]
              :muuntaja muuntaja/instance}})))

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

(deftest echo-token-test
  #_(testing "get value when value exists"
      (is (string? (cmr/echo-token :prod))))
  (testing "returns nil when no token is available"
    (is (nil? (cmr/echo-token :elsewhere)))))


(deftest search-test
  (testing "returns a search request command"
    (are [concept-type opts url]
        (let [request (cmr/search concept-type nil opts)]
          (is (= :get (:method request)))
          (is (= url (:url request))))
      :granule {:format :echo10} "/search/granules.echo10"
      :granule {} "/search/granules"
      :collection {:format :umm_json} "/search/collections.umm_json"
      :collection {:format :iso} "/search/collections.iso"
      :service {} "/search/services"
      :tool {} "/search/tools")))
