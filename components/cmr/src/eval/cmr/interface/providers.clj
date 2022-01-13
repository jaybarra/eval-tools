(ns eval.cmr.interface.providers
  (:require
   [eval.cmr.commands.providers :as providers]))

(defn get-providers
  []
  (providers/get-providers))

(defn create-provider
  "Returns a command to create a provider"
  [provider options]
  (providers/create-provider provider options))
