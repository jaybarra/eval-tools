(ns eval.api.providers
  (:require
   [eval.services.cmr.providers :as providers]
   [ring.util.response :refer [response]]))

(defn get-providers
  [request]
  (let [client (get-in request [:cmr :instances :local])
        cmr-response (providers/get-providers client)]
    (response cmr-response)))

(def routes
  ["/providers" {:get {:handler get-providers}}])
