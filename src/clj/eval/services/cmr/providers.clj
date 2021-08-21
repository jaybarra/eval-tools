(ns eval.services.cmr.providers
  (:require
   [eval.cmr.core :as cmr]
   [eval.cmr.providers :as providers]))

(defn create-provider
  [client provider & [opts]]
  (->> (providers/create-provider provider opts)
       (cmr/invoke client)
       cmr/decode-cmr-response-body))

(defn get-providers
  [client]
  (->> (providers/get-providers)
       (cmr/invoke client)
       cmr/decode-cmr-response-body))
