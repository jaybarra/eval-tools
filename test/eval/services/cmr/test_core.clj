(ns eval.services.cmr.test-core
  "Test utilities for the CMR services"
  (:require
   [eval.cmr.core :as cmr]
   [jsonista.core :as json]))

(defn client
  "Returns a reified CMR client, useful for mocking responses from CMR"
  [invoke-fn echo-fn]

  (reify cmr/CmrClient

    (-invoke [client command]
      (invoke-fn client command))

    (-echo-token [client]
      (echo-fn client))))

(defn context
  "Returns a mock system context."
  [& args]
  (let [clients (apply merge (for [[label client] (partition 2 args)] {label client}))]
    {:app/cmr {:instances clients}}))
