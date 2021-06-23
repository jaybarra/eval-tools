(ns eval.cmr.acls
  "Namespace for interacting with the access-control service. Defines
  commands and queries for use with the [[cmr/invoke]] function.

  ## Example usage
  (require '[eval.cmr.core :as cmr])
  (require '[eval.cmr.acls :as acl)
  (def client (cmr/create-client
               {:id :prod
                :url \"https://cmr.earthdata.nasa.gov\"})
  (cmr/invoke client (acl/get-acls))"
  (:require
   [clojure.spec.alpha :as spec]
   [eval.cmr.core :as cmr]))

(spec/def ::group (spec/keys :req-un [::name]
                             :opt-un [::description
                                      ::members
                                      ::provider_id]))

(spec/def ::permissions #{"create" "read" "update" "order"})
(spec/def ::group_id string?)
(spec/def ::group-permission (spec/keys :req-un [::group_id ::permissions]))
(spec/def ::group_permissions (spec/* ::group-permission))
(spec/def ::catalog_item_identity map?)
(spec/def ::acl (spec/keys :req-un [::group_permissions
                                    ::catalog_item_identity]))

(defn get-groups
  [& [query]]
  (let [request {:method :get
                 :url "/access-control/groups"}]
    (if query
      (assoc request :query-params query)
      request)))

(defn create-group
  [group]
  {:method :post
   :url "/access-control/groups"
   :headers {"Content-Type" "application/json"}
   :body (cmr/encode->json group)})

(defn get-group
  "Constructs a query for getting a group by id.

  Options
  pretty | boolean"
  [group-id & [opts]]
  (let [req {:method :get
             :url (str "/access-control/groups/" group-id)}]
    (if-let [pretty (get opts :pretty)]
      (assoc-in req [:query-params :pretty] pretty)
      req)))

(defn delete-group
  "Constructs a query to delete a group by id."
  [group-id]
  {:method :delete
   :url (str "/access-control/groups/" group-id)})

(defn update-group
  "Constructs a query to update a group."
  [group-id group]
  {:method :put
   :url (str "/access-control/groups/" group-id)
   :headers {"Content-Type" "application/json"}
   :body (cmr/encode->json group)})

(defn get-group-members
  [group-id & [opts]]
  (let [req {:method :get
             :url (str "/access-control/groups/" group-id "/members")}]
    (if-let [pretty (get opts :pretty false)]
      (assoc-in req [:query-params :pretty] pretty)
      req)))

(defn remove-group-members
  [group-id users]
  {:method :delete
   :url (str "/access-control/groups/" group-id "/members")
   :headers {"Content-Type" "application/json"}
   :body (cmr/encode->json users)})

(defn get-acls
  "Return a query for requesting ACLs from "
  [& [query]]
  (let [request {:method :get
                 :url "/access-control/acls"}]
    (if query
      (assoc request :query-params query)
      request)))

(defn create-acl
  "Return a query for requesting ACLs from "
  [acl]
  {:method :post
   :url "/access-control/acls"
   :headers {"Content-Type" "application/json"}
   :body (cmr/encode->json acl)})

(defn get-permissions
  [query]
  {:method :get
   :url (str "/access-control/permissions")
   :query-params query})

(defn get-s3-buckets
  [user-id & [providers]]
  (let [query {:method :get
               :url "/access-control/s3-buckets"
               :query-params {:user-id user-id}}]
    (if providers
      (assoc-in query [:query-params :provider] providers)
      query)))

(defn health
  [& [opts]]
  (let [req {:method :get
             :url "/access-control/health"}]
    (if-let [pretty (get opts :pretty)]
      (assoc-in req [:query-params :pretty] pretty)
      req)))
