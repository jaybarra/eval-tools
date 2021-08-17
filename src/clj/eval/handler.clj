(ns eval.handler
  "Core web handler"
  (:require
   [clojure.java.io :as io]

   [eval.api.health :as health]
   [eval.api.cmr :as cmr]

   [integrant.core :as ig]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.middleware :as middleware]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [taoensso.timbre :as log]))

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

(defn cmr-middlware-wrapper
  [handler cmr]
  (fn [request]
    (handler (assoc request :cmr cmr))))

(def cmr-middleware
  {:name ::cmr
   :description "add cmr context to requests"
   :wrap cmr-middlware-wrapper})

(derive ::not-found ::exception)
(derive ::failure ::exception)
(derive ::error ::exception)

(defn local-error-handler
  [message ex request]
  {:status 500
   :body {:message message
          ;; :exception (.getClass ex)
          :data (ex-data ex)
          :uri (:uri request)}})

(def cmr-exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {:cmr (partial local-error-handler "CMR Error")})))

(defn create-app
  "Return a configured handler instance."
  [cmr]
  (ring/ring-handler
   (ring/router
    ["/api" [cmr/routes
             health/routes]]
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

                         ;; Custom middleware
                         cors-middleware
                         [cmr-middleware cmr]
                         cmr-exception-middleware]}
     :exception pretty/exception})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))))
