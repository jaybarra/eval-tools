(ns eval.services.cmr.ingest
  (:require
   [eval.cmr.core :as cmr]
   [eval.cmr.commands.ingest :as ingest]))

(defn upload-collection
  "Submit a collection to CMR and return the response."
  [client provider-id collection]
  (cmr/invoke client
              (ingest/create-concept :collection
                                     provider-id
                                     collection
                                     {:format :umm-json})))
