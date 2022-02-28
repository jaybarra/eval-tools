(ns eval.cache.core
  (:require
   [jsonista.core :as json]
   [taoensso.carmine :as car]))

(def server1-conn {:pool {}
                   :spec {:uri "redis://localhost:6379/"}})

(defmacro wcar*
  [& body]
  `(car/wcar server1-conn ~@body))

(defonce expiration-listener
  (car/with-new-pubsub-listener
    (:spec server1-conn)
    {(name :expiration-events) (fn [msg] (println "-->" msg))}
    (car/subscribe (name :expiration-events))))

(comment
  
  (wcar* (car/publish (name :expiration-events) "a thing is sent"))


  (spit "kms.json" (json/write-value-as-string (wcar* (car/get ":kms"))))

  (wcar* (car/del "\":kms-hash-code\""))
  (wcar* (car/set "\":kms-hash-code\"" {:value -567107498}))
  )
