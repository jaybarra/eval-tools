(ns eval.services.cmr.search
  (:require
   [clojure.core.async :as a]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [eval.cmr.core :as cmr]
   [taoensso.timbre :as log]))

(defn search
  [context cmr-inst concept-type query & [opts]]
  (let [{:keys []
         {:keys [db connections]} :app/cmr} context
        client (get connections cmr-inst)]
    (cmr/search client concept-type opts)))
