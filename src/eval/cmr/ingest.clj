(ns eval.cmr.ingest
  (:require
   [clj-http.client :as client]
   [eval.cmr.core :as cmr :refer [state->cmr]]
   [muuntaja.core :as m]
   [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Specs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti ingest!
  (fn [_context concept]
    (:concept-type concept)))

(defmethod ingest! :default
  [_context concept]
  (throw (ex-info "No ingest handler for concept-type" (:concept-type concept)
                  {:concept concept})))

(defmethod ingest! :collection
  [context concept]
  (let [{cmr-url ::cmr/url
         cmr-env ::cmr/env} (state->cmr context)
        ingest-url (format "%s/ingest" cmr-url)
        echo-token (cmr/echo-token cmr-env) ]
    (time (m/decode-response-body
           (client/post ingest-url
                        {:headers {"Echo-Token" echo-token}
                         :body concept})))))
