(ns eval.cmr.ingest
  "Ingest commands for use with [[cmr/invoke]]."
  (:require
   [eval.cmr.core :as cmr]))

(defn- update-req-content-type
  "Set the appropriate Content-Type header based on the concept format."
  [req fmt]
  (assoc-in req [:headers "Content-Type"] (cmr/format->mime-type fmt)))

(defn validate-concept-metadata
  "Returns a command to validate a given concept.
  Supported concept-types:
  + collection
  + granule"
  [concept-type provider-id concept & [{fmt :format}]]
  (let [req {:method :post
             :url (format "/ingest/providers/%s/validate/%s/%s"
                          provider-id
                          (name concept-type)
                          (:native-id concept))
             :body concept}
        request (update-req-content-type req fmt)]
    {:request request}))

(defn create-concept
  "Returns a command to create a given concept."
  [concept-type provider-id concept & [{fmt :format native-id :native-id}]]
  (let [req {:method :put
             :url (format "/ingest/providers/%s/%s%s"
                          provider-id
                          (str (name concept-type) "s")
                          (if native-id (str "/" native-id) ""))
             :body concept}
        request (update-req-content-type req fmt)]
    {:request request}))

(def update-concept
  "Alias for [[create-concept]]"
  create-concept)

(defn delete-concept
  "Mark a concept as deleted in CMR"
  [concept-type provider-id concept-native-id]
  {:request
   {:method :delete
    :url (format "/ingest/providers/%s/%s/%s"
                 provider-id
                 (str (name concept-type) "s")
                 concept-native-id)}})

(defn create-association
  "Create an association beteween collection and variable"
  [collection-id collection-revision variable-id]
  {:request
   {:method :put
    :url (format "/ingest/collections/%s/%s/variables/%s"
                 collection-id
                 collection-revision
                 variable-id)}})
