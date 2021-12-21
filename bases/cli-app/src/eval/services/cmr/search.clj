(ns eval.services.cmr.search
  (:require
   [eval.cmr.commands.search :as search-api]
   [eval.cmr.core :as cmr]))

(defn search
  "Search concepts for concepts"
  [client concept-type query & [opts]]
  (cmr/decode-cmr-response-body
   (cmr/invoke client (search-api/search concept-type query opts))))

(defn query-hits
  "Query CMR for count of concepts available that are available from
  a given query.

  Takes a query and sets a :page_size of 0 and returns
  the CMR-Hits header string as an integer value."
  [client concept-type query & [opts]]
  (let [query (-> query
                  (as-> q (search-api/search concept-type q opts))
                  (assoc :page_size 0))]
    (-> (cmr/invoke client query)
        (get-in [:headers :CMR-Hits])
        Integer/parseInt)))
