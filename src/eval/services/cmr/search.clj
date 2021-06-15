(ns eval.services.cmr.search
  (:require
   [clojure.core.async :as a]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [eval.cmr.core :as cmr]
   [taoensso.timbre :as log]))

(defn search
  "Search concepts for concepts"
  [context cmr-inst concept-type query & [opts]]
  (let [client (cmr/context->client context cmr-inst)]
    (cmr/decode-cmr-response-body
     (cmr/search client concept-type query opts))))
