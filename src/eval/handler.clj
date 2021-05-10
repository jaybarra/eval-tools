(ns eval.handler
  (:require
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.dev.pretty :as pretty]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring :as ring]
   [ring.middleware.params :as params]
   [ring.util.response :refer [response status]]))

(def ok-handler (constantly (status 200)))

(def routes
  [["/" {:get {:handler ok-handler}}]])

(defn create-app
  []
  (ring/ring-handler
   (ring/router
    routes
    {:data {:coercion reitit.coercion.spec/coercion
            :middleware [params/wrap-params
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         muuntaja/format-request-middleware
                         coercion/coerce-request-middleware
                         coercion/coerce-response-middleware]
            :muuntaja m/instance}
     :exception pretty/exception})
   (ring/create-default-handler)))
