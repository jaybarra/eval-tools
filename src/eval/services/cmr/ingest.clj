(ns eval.services.cmr.ingest
  (:require
   [eval.cmr.core :as cmr]
   [eval.cmr.ingest :as ingest]
   [eval.services.cmr.core :refer [context->client]]))

(defn upload-collection
  [context cmr-inst provider-id collection & [opts]]
  (let [client (context->client context cmr-inst)]
    (cmr/invoke client (ingest/create-concept :collection
                                              provider-id
                                              collection
                                              {:format :umm-json}))))
