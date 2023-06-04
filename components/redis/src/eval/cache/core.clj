(ns eval.cache.core
  (:require
   [jsonista.core :as json]
   [taoensso.carmine :as car]))

(comment
  (car/wcar {:pool {}
             :spec {:uri "redis://localhost:6379"}}
            (car/get ":kms")))
