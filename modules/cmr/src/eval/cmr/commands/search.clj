(ns eval.cmr.commands.search
  (:require
   [clojure.string :as str]
   [eval.cmr.core :as cmr]))

(defn search
  "Returns a query for a specific concept-type"
  [concept-type query & [opts]]

  (let [concept-name (name concept-type)
        path (if (str/ends-with? concept-name "s")
               concept-name
               (str concept-name "s"))
        search-url (format
                    "/search/%s%s"
                    path
                    (cmr/format->cmr-url-extension (:format opts)))
        command {:request {:method :get
                           :url search-url
                           :query-params query}}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn ^{:deprecated true :superseded-by "search-after"} scroll
  "Returns a query with a scroll-id in the header. 
  The output will be identical to a [[search]] response."
  [concept-type query scroll-id & [opts]]
  (assoc-in (search concept-type query opts)
            [:request :headers "CMR-Scroll-Id"]
            scroll-id))

(defn clear-scroll-session
  "Returns a query that will clear a specific scroll-id session."
  [scroll-id]
  {:request
   {:method :post
    :url "/search/clear-scroll"
    :headers {:content-type "application/json"}
    :body (cmr/encode->json {:scroll_id (str scroll-id)})}})

(defn search-after
  "Returns a query with a CMR-Search-After in the header for use in harvesting queries."
  [concept-type query sa-key & [opts]]
  (assoc-in (search concept-type query opts)
            [:request :headers "CMR-Search-After"]
            sa-key))
