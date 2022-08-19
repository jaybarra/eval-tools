(ns eval.cmr-dash.core
  (:gen-class)
  (:require
   [muuntaja.core :as m]
   [org.httpkit.client :as client]
   [org.httpkit.server :refer [run-server]]
   [org.httpkit.sni-client :as sni-client]
   [reitit.ring :as ring]
   [ring.util.http-response :as response :refer [ok]]
   [reitit.ring.coercion :as coercion :refer [coerce-response-middleware
                                              coerce-request-middleware
                                              coerce-exceptions-middleware]]
   [reitit.ring.middleware.exception :refer [exception-middleware]]
   [reitit.ring.middleware.muuntaja :refer [format-negotiate-middleware
                                            format-request-middleware
                                            format-response-middleware]]

   ;; relocate to component
   [selmer.parser :as selmer])
  (:import
   [java.time Instant ZoneOffset]
   [java.util UUID]))

;; required for Java 8
(alter-var-root #'org.httpkit.client/*default-client* (fn [_] sni-client/default-client))

(defn wrap-init-request
  [handler]
  (fn [req]
    (handler (assoc req :request-id (UUID/randomUUID)))))

(def app
  (ring/ring-handler
   (ring/router
    [["/status" {:get
                 (fn [_req]
                   (let [now (Instant/now)
                         {:keys [status headers body error opts] :as resp} @(client/request {:url "https://cmr.earthdata.nasa.gov/search/health"})]
                     (if error
                       (ok
                        (selmer/render-file "templates/cmr-status.html" {:status "degraded"
                                                                         :instance "PROD"
                                                                         ;; Java 9+ compatible
                                                                         ;; :date (LocalDate/ofInstant now)
                                                                         :date (.toLocalDate (.atZone now ZoneOffset/UTC))
                                                                         :timestamp now}))
                       (ok
                        (selmer/render-file "templates/cmr-status.html" {:status "good"
                                                                         :instance "PROD"
                                                                         ;; Java 9+ compatible
                                                                         ;; :date (LocalDate/ofInstant now)
                                                                         :date (.toLocalDate (.atZone now ZoneOffset/UTC))
                                                                         :timestamp now})))))}]
     ["/api" {:get (fn [_req]
                     {:status 200
                      :body {:message "ok"}})}]]
    {:data {:muuntaja m/instance
            :middleware [format-negotiate-middleware
                         format-response-middleware
                         exception-middleware
                         format-request-middleware
                         wrap-init-request]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404
                              :body "Not Found"})}))))

(defonce server (atom nil))
(defonce options (atom {}))

(defn start-server
  [opts]
  (let [{:keys [port]} opts
        port (or port 7999)]
    (reset! options {:port port})
    (reset! server (run-server app @options))))

(defn stop-server
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn restart-server
  []
  (stop-server)
  (start-server @options))

(defn -main
  [& args]
  (start-server args))

(comment
  (restart-server)
  (app {:request-method :get
        :uri "/api"})

  (app {:request-method :get
        :uri "/status"}))
