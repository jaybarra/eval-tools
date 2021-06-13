(ns eval.cmr.ingest
  (:require
   [eval.cmr.core :as cmr]))

(defn validate-metadata
  [client concept-type provider-id granule & [opts]]
  {:method :post
   :url (format "/providers/%s/validate/%s/%s"
                provider-id
                (name concept-type)
                (:native-id granule))})

(defn create-concept
  [client concept-type provider-id concept & [native-id]]
  {:method :put
   :url (format "/providers/%s/%s%s"
                provider-id
                (str (name concept-type) "s")
                (or (str "/" native-id) ""))
   :body (cmr/encode->json concept)})

(def update-concept
  "Alias for [[create-concept]]"
  create-concept)

(defn delete-concept
  [client concept-type provider-id concept-native-id]
  {:method :delete
   :url (format "/providers/%s/%s/%s"
                provider-id
                (str (name concept-type) "s")
                concept-native-id)})

(defn create-association
  [client collection-id collection-revision variable-id]
  {:method :put
   :url (format "/collections/%s/%s/variables/%s"
                collection-id
                collection-revision
                variable-id)})

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
;; /providers//bulk-update/collections
;; POST - Collection bulk update
;; /providers//bulk-update/granules
;; POST - Granule bulk update
