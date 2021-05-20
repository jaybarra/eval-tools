(ns eval.api.home
  (:require
   [clj-http.client :as http]
   [taoensso.timbre :as log]))

(defn cmr-up?
  [cmr-inst]
  (try
    (http/get "https://cmr.earthdata.nasa.gov/search/health")
    (catch Exception e
      (log/error "Could not reach CMR" e)
      false)))
