(ns eval.cmr.search
  (:require
   [eval.cmr.core :as cmr]))

(defn search
  "Returns a query for a specific concept-type"
  [concept-type query & [opts]]
  (let [search-url (format
                    "/search/%ss%s"
                    (name concept-type)
                    (cmr/format->cmr-extension (:format opts)))
        command {:request {:method :get
                           :url search-url
                           :query-params query}}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn scroll
  "Returns a query with a scroll-id in the header. The output will be
  identical to a [[search]] response."
  [concept-type query scroll-id & [opts]]
  (assoc-in (search concept-type query opts)
            [:request :headers "CMR-Scroll-Id"]
            scroll-id))

(defn clear-scroll-session
  "Returns a query that will clear a specific scroll-id session."
  [scroll-id]
  {:request {:method :post
             :url "/search/clear-scroll"
             :headers {:content-type "application/json"}
             :body (cmr/encode->json {:scroll_id scroll-id})}})
