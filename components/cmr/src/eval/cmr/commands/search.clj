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

(defn search-after
  "Returns a query with a CMR-Search-After in the header for use in harvesting queries."
  [concept-type query sa-key & [opts]]
  (if (seq sa-key)
    (-> (search concept-type query opts)
        (assoc-in [::cmr/request :headers "CMR-Search-After"] sa-key))
    (search concept-type query opts)))

(defn search-after-post
  "Returns a query with a CMR-Search-After in the header for use in harvesting queries."
  [concept-type query sa-key & [opts]]
  (if (seq sa-key)
    (-> (search-post concept-type query opts)
        (assoc-in [::cmr/request :headers "CMR-Search-After"] sa-key))
    (search-post concept-type query opts)))

(defn fetch-community-usage-metrics
  "Returns a query for the community usage metrics"
  []
  {::cmr/request
   {:method :get
    :url "/search/community-usage-metrics"}
   ::cmr/category :read})
