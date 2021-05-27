(ns eval.cmr.acls
  (:require
   [clojure.core.async :as a]
   [eval.cmr.core :as cmr]))

(defn acls-query
  "Return a query for requesting ACLs from "
  [& [query]]
  (let [request {:method :get
                 :url "/access-control/acls"}]
    (if query
      (assoc request :query-params query)
      request)))

;; do not like the "invoke" in the middle
;; would prefer two-phase handler request, response, tied together with async-channel
(defn get-acls
  [client]
  (cmr/umm-json-response->items (cmr/invoke client (acls-query))))
