(ns eval.cmr.commands.ingest
  "Ingest commands for use with [[cmr/invoke]]."
  (:require
   [eval.cmr.client :as cmr]))

(defn- update-req-content-type
  "Set the appropriate Content-Type header based on the concept format."
  [req fmt]
  (update-in req
             [:headers]
             merge
             (when-let [fmt-str (cmr/format->mime-type fmt)]
               {:content-type fmt-str})))

(defn validate-concept-metadata
  "Returns a command to validate a given concept.
  Supported concept-types:
  + collection
  + granule"
  [concept-type provider-id native-id concept-data & [{fmt :format}]]
  (let [req {:method :post
             :url (format "/ingest/providers/%s/validate/%s/%s"
                          provider-id
                          (name concept-type)
                          native-id)
             :body concept-data}
        request (update-req-content-type req fmt)]
    {::cmr/request request
     ::cmr/category :check}))

(defn create-concept
  "Returns a command to create a given concept."
  [concept-type provider-id concept & [{fmt :format native-id :native-id :as opts}]]
  (let [req {:method :put
             :url (format "/ingest/providers/%s/%s%s"
                          provider-id
                          (str (name concept-type) "s")
                          (if native-id (str "/" native-id) ""))
             :body concept}
        req (merge req (when-let [headers (:headers opts)] {:headers headers}))
        request (update-req-content-type req fmt)]
    {::cmr/request request
     ::cmr/category :create}))

(defn update-concept
  "Alias for [[create-concept]]"
  [concept-type provider-id concept & [opts]]
  (assoc (create-concept concept-type provider-id concept opts)
         ::cmr/category :update))

(defn delete-concept
  "Mark a concept as deleted in CMR"
  [concept-type provider-id concept-native-id]
  {::cmr/request
   {:method :delete
    :url (format "/ingest/providers/%s/%s/%s"
                 provider-id
                 (str (name concept-type) "s")
                 concept-native-id)}
   ::cmr/category :delete})

(defn create-association
  "Create an association beteween collection and variable"
  [collection-id collection-revision variable-id]
  {::cmr/request
   {:method :put
    :url (format "/ingest/collections/%s/%s/variables/%s"
                 collection-id
                 collection-revision
                 variable-id)}
   ::cmr/category :create})
