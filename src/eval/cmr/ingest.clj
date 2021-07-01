(ns eval.cmr.ingest
  "Ingest commands for use with [[cmr/invoke]]."
  (:require
   [eval.cmr.core :as cmr]))

(defn validate-concept-metadata
  "Returns a command to validate a given concept."
  [concept-type provider-id concept & [opts]]
  {:method :post
   :url (format "/ingest/providers/%s/validate/%s/%s"
                provider-id
                (name concept-type)
                (:native-id concept))
   :headers {"Content-Type" (cmr/format->mime-type (:format opts))}
   :body (if (some #{:umm-json :json} [(:format opts)])
           (cmr/encode->json concept)
           concept)})

(defn create-concept
  "Retrurns a command to create a given concept."
  [concept-type provider-id concept & [native-id]]
  {:method :put
   :url (format "/ingest/providers/%s/%s%s"
                provider-id
                (str (name concept-type) "s")
                (if native-id (str "/" native-id) ""))
   :body (cmr/encode->json concept)})

(def update-concept
  "Alias for [[create-concept]]"
  create-concept)

(defn delete-concept
  [concept-type provider-id concept-native-id]
  {:method :delete
   :url (format "/ingest/providers/%s/%s/%s"
                provider-id
                (str (name concept-type) "s")
                concept-native-id)})

(defn create-association
  [collection-id collection-revision variable-id]
  {:method :put
   :url (format "/ingest/collections/%s/%s/variables/%s"
                collection-id
                collection-revision
                variable-id)})




;; /providers/<provider-id>/collections/<native-id>
;; PUT - Create or update a collection.
;; DELETE - Delete a collection.

;; /providers/<provider-id>/granules/<native-id>
;; PUT - Create or update a granule.
;; DELETE - Delete a granule.
;; /collections/<collection-concept-id>/<collection-revision-id>/variables/<native-id>
;; PUT - Create or update a variable with assoication.
;; /providers/<provider-id>/variables/<native-id>
;; PUT - Update a variable.
;; DELETE - Delete a variable.
;; /providers/<provider-id>/services/<native-id>
;; PUT - Create or update a service.
;; DELETE - Delete a service.
;; /providers/<provider-id>/tools/<native-id>
;; PUT - Create or update a tool.
;; DELETE - Delete a tool.
;; /providers/<provider-id>/subscriptions
;; POST - Create a subscription without specifying a native-id.
;; /providers/<provider-id>/subscriptions/<native-id>
;; POST - Create a subscription with a provided native-id.
;; PUT - Create or Update a subscription.
;; DELETE - Delete a subscription.
;; Subscription Access Control
;; /translate/collection
;; POST - Translate collection metadata.
;; /translate/granule
;; POST - Translate granule metadata.
;; /providers/<provider-id>/bulk-update/collections
;; POST - Collection bulk update
;; /providers/<provider-id>/bulk-update/granules



;; POST - Granule bulk update
;; /granule-bulk-update/status
;; POST - Granule bulk update
;; /granule-bulk-update/status/<task-id>
;; GET - Granule bulk update status
