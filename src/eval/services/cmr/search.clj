(ns eval.services.cmr.search
  (:require
   [clojure.core.async :as a]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [eval.cmr.core :as cmr]
   [taoensso.timbre :as log]))

(defn search
  "Search send a query to a CMR instance and return the response."
  [context cmr-inst concept-type query & [opts]]
  (let [client (cmr/context->client context cmr-inst)]
    (cmr/search client concept-type opts)))
