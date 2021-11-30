(ns eval.services.cmr.providers
  (:require
   [eval.cmr.commands.providers :as providers]
   [eval.cmr.core :as cmr]))

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
