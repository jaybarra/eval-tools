(ns eval.cmr.interface.client
  "Default functionality for interacting with a Common Metadata Repository instance.

  This namespace provides basic interaction with a CMR instance through the [[eval.cmr.client/invoke]] function."
  (:require
   [eval.cmr.client :as client]))

(defn create-client
  [options]
  (client/create-client options))

(defn invoke
  [client command]
  (client/invoke client command))
