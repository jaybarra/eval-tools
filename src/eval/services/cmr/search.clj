(ns eval.services.cmr.search
  (:require
   [clojure.core.async :as a]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [eval.cmr.core :as cmr-api]
   [eval.cmr.search :as cmr-search]
   [eval.services.cmr.core :as cmr]
   [taoensso.timbre :as log]))

(defn search
  "Search concepts for concepts"
  [context cmr-inst concept-type query & [opts]]
  (let [client (cmr/context->client context cmr-inst)]
    (cmr-api/decode-cmr-response-body
     (cmr-api/invoke client (cmr-search/search concept-type query opts)))))

(defn query-hits
  "Query CMR for count of available concepts that are available from
  a given query.

  Takes a query and sets a :page_size of 0 and returns
  the CMR-Hits header string as an integer value."
  [context cmr-inst concept-type query & [opts]]
  (let [client (cmr/context->client context cmr-inst)
        query (-> query
                  (as-> q (cmr-search/search concept-type q opts))
                  (assoc :page_size 0))]
    (-> (cmr-api/invoke client query)
        (get-in [:headers :CMR-Hits])
        Integer/parseInt)))

(defn clear-scroll-session!
  [context cmr-inst session-id]
  (let [client (cmr/context->client context cmr-inst)]
    (cmr-api/invoke client (cmr-search/clear-scroll-session session-id))))

(defn scroll!
  "Begin or continue a scrolling session and returns a map with
  :CMR-Scroll-Id and :response.

  ## CMR-Scroll-Id

  The first scroll! query will return the CMR-Scroll-Id in the header
  of the response. Add this to the options map of subsequent calls to
  continue getting results.

  e.g.
  %> (scroll! cmr :granules query)
  {:CMR-Scroll-Id \"612341\"
   :response <page 1 of results>}

  %> (scroll! cmr :granules query {:CMR-Scroll-Id \"612341\"})
  {:CMR-Scroll-Id \"612341\"
   :response <page 2 of results>}

  %> (get-in (clear-scroll-session! cmr \"612341\") :status)
  204

  The ideal use case is to always run in a try-finally
  e.g.

  (try
    (scroll! cmr query {:CMR-Scroll-id scroll-id)
    (finally (clear-scroll-session! cmr scroll-id))

  ## Query Parameters
  Standard [[search]] parameters are accepted with the following exceptions.

  :page_num and :offset are not valid params when using the scrolling endpoint.
  :page_size is a valid query param and must be below 2000

  Repeated calls will yield additional results.

  Be sure to call [[clear-scroll-session!]] when finished. "
  [context cmr-inst concept-type query & [opts]]
  (let [client (cmr/context->client context cmr-inst)
        scroll-query (-> query
                         (dissoc :page_num :offset)
                         (assoc :scroll true))
        existing-scroll-id (:CMR-Scroll-Id opts)
        request (cmr-search/search concept-type scroll-query opts)
        scroll-request (cond-> request
                         existing-scroll-id (assoc-in
                                             [:headers :CMR-Scroll-Id]
                                             existing-scroll-id))
        response (cmr-api/invoke client scroll-request opts)
        scroll-id (get-in response [:headers :CMR-Scroll-Id])]
    (if existing-scroll-id
      (log/debug "Continuing scroll [" scroll-id "]")
      (log/debug "Started new scroll session [" scroll-id "]"))
    {:CMR-Scroll-Id scroll-id
     :response response}))
