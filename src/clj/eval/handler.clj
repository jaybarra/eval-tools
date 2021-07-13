(ns eval.handler
  "Core web handler"
  (:require
   [clojure.java.io :as io]
   [eval.api.health :as health]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.middleware :as middleware]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]))

(defn cors-middleware-wrapper
  [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Access-Control-Allow-Origin"] "*"))))

(def cors-middleware
  {:name ::cors
   :description "Apply CORS headers to responses"
   :wrap cors-middleware-wrapper})

(defn create-app
  "Return a configured handler instance."
  []
  (ring/ring-handler
   (ring/router
    ["/api" [health/routes]]
    {:data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [parameters/parameters-middleware
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         muuntaja/format-request-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware
                         multipart/multipart-middleware
                         cors-middleware]}
     :exception pretty/exception})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))))
