(ns eval.cmr.search
  (:require
   [eval.cmr.core :as cmr]))

(defn search
  "GET the collections from the specified CMR enviroment.

  Send a GET request to the search endpoint for the specific concept-type
  as a query-param."
  ([concept-type query & [opts]]
   (let [search-url (format
                     "/search/%ss%s"
                     (name concept-type)
                     (cmr/format->cmr-extension (:format opts)))]
     {:method :get
      :url search-url
      :query-params query})))

(defn clear-scroll-session
  "Clear the CMR scroll session."
  [scroll-id]
  {:method :post
   :url "/search/clear-scroll"
   :headers {:content-type "application/json"}
   :body (cmr/encode->json {:scroll_id scroll-id})})
