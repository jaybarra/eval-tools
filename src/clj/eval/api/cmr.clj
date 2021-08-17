(ns eval.api.cmr
  (:require
   [clj-http.client :as http]
   [clojure.spec.alpha :as spec]
   [clojure.walk :as walk]
   [eval.services.cmr.providers :as providers]
   [taoensso.timbre :as log]
   [ring.util.response :refer [response not-found]]))

(defn cmr-not-found-wrapper
  "Middleware to check a given cmr-inst exists in the system"
  [handler]
  (fn [{:keys [cmr path-params] :as request}]
    (let [cmr-inst (keyword (get path-params :cmr-inst))]
      (if-not (get-in cmr [:instances cmr-inst])
        (not-found {:error "No such CMR instance"
                    :id cmr-inst})
        (handler request)))))

(defn get-config
  "Return the configured CMR clients"
  [request]
  (let [cmr (get-in request [:cmr :instances])]
    (response cmr)))

(defn get-providers
  "Return a list of providers from a CMR instance"
  [request]
  (let [cmr-inst (get-in request [:path-params :cmr-inst])
        client (get-in request [:cmr :instances (keyword cmr-inst)])]
    (response (providers/get-providers client))))

(def routes
  ["/cmr"
   ["/overview" {:handler get-config}]
   ["/:cmr-inst" {:middleware [cmr-not-found-wrapper]}
    ["/providers" {:handler get-providers}]]])
