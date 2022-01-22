(ns eval.kibana.core
  (:require
   [clj-http.client :as client]))

(defn submit-saved-object
  [conn saved-object]
  (let [url (str (:url conn) "/api/saved_objects/_import")]
    (client/post url saved-object)))
