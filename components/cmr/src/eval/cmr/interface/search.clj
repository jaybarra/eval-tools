(ns eval.cmr.interface.search
  (:require
   [eval.cmr.commands.search :as search]))

(defn search
  "Returns a query for a specific concept-type"
  [concept-type query options]
  (search/search concept-type query options))

(defn search-after
  "Returns a query with a CMR-Search-After in the header for use in harvesting queries."
  [concept-type query sa-key options]
  (search/search-after concept-type
                       query
                       sa-key
                       options))

(defn fetch-community-usage-metrics
  "Returns a query with to fetch the current community usage metrics."
  []
  (search/fetch-community-usage-metrics))

(defn search-post
  "Returns a query that uses POST."
  [concept-type query options]
  (search/search-post concept-type query options))

(defn search-after-post
  "Returns a serach-after query that uses POST."
  [concept-type query sa-key options]
  (search/search-after-post concept-type query sa-key options))
