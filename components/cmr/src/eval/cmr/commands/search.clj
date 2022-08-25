(ns eval.cmr.commands.search
  (:require
   [clojure.string :as str]
   [eval.cmr.client :as cmr]))

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
        command {::cmr/request {:method :get
                                :url search-url
                                :query-params query}
                 ::cmr/category :read}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn search-post
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
        command {::cmr/request {:method :post
                                :url search-url
                                :multipart query}
                 ::cmr/category :read}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn ^{:deprecated true
        :superseded-by "search-after"} scroll
  "Returns a query with a scroll-id in the header.
  The output will be identical to a [[search]] response."
  [concept-type query scroll-id & [opts]]
  (-> (search concept-type query opts)
      (assoc-in
       [::cmr/request :headers "CMR-Scroll-Id"]
       scroll-id)))

(defn clear-scroll-session
  "Returns a query that will clear a specific scroll-id session."
  [scroll-id]
  {::cmr/request
   {:method :post
    :url "/search/clear-scroll"
    :headers {:content-type "application/json"}
    :body {:scroll_id (str scroll-id)}}
   ::cmr/category :read})

(defn search-after
  "Returns a query with a CMR-Search-After in the header for use in harvesting queries."
  [concept-type query sa-key & [opts]]
  (-> (search concept-type query opts)
      (assoc-in [::cmr/request :headers "CMR-Search-After"] sa-key)))

(defn search-after-post
  "Returns a query with a CMR-Search-After in the header for use in harvesting queries."
  [concept-type query sa-key & [opts]]
  (-> (search-post concept-type query opts)
      (assoc-in [::cmr/request :headers "CMR-Search-After"] sa-key)))

(defn fetch-community-usage-metrics
  "Returns a query for the community usage metrics"
  []
  {::cmr/request
   {:method :get
    :url "/search/community-usage-metrics"}
   ::cmr/category :read})
