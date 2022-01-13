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
