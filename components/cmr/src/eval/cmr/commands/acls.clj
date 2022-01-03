(ns eval.cmr.commands.acls
  "Namespace for interacting with the ACLs, groups, and permissions. 
  Defines commands and queries for use with the [[eval.cmr.client/invoke]] function.

  ## Example usage
  ```clojure
  (require '[eval.cmr.client :as cmr])
  (require '[eval.cmr.commands.acls :as acl)
  (def client (cmr/create-client
               {:id :prod
                :url \"https://cmr.earthdata.nasa.gov\"})
  (cmr/invoke client (acl/get-acls))
   ```"
  (:require
   [clojure.spec.alpha :as spec]
   [eval.cmr.client :as cmr]))

(spec/def ::group (spec/keys :req-un [::name]
                             :opt-un [::description
                                      ::members
                                      ::provider_id]))
(spec/def ::group_id string?)
(spec/def ::permissions #{"create" "read" "update" "order"})
(spec/def ::group-permission (spec/keys :req-un [::group_id
                                                 ::permissions]))
(spec/def ::group_permissions (spec/* ::group-permission))
(spec/def ::catalog_item_identity map?)
(spec/def ::acl (spec/keys :req-un [::group_permissions
                                    ::catalog_item_identity]))

(defn get-groups
  [& [query options]]
  (let [request {:method :get
                 :url "/access-control/groups"}
        request (if query (assoc request :query-params query) request)]
    (merge
      {::cmr/request request
       ::cmr/category :read}
     (when options {:opts options}))))

(defn create-group
  [group & [options]]
  (let [command {::cmr/request {:method :post
                                :url "/access-control/groups"
                                :headers {"Content-Type" "application/json"}
                                :body group}
                 ::cmr/category :create}]
    (merge command (when options {:opts options}))))

(defn get-group
  "Constructs a query for getting a group by id."
  [group-id & [options]]
  (let [request {:method :get
                 :query-params {:pretty true}
                 :url (str "/access-control/groups/" group-id)}]
    {::cmr/request request
     ::cmr/category :read}))

(defn delete-group
  "Constructs a query to delete a group by id."
  [group-id & [options]]
  (let [request {:method :delete
                 :url (str "/access-control/groups/" group-id)}
        command {::cmr/request request
                 ::cmr/category :delete}]
    (merge command (when options {:opts options}))))

(defn update-group
  "Constructs a query to update a group."
  [group-id group & [options]]
  (let [request {:method :put
                 :url (str "/access-control/groups/" group-id)
                 :headers {"Content-Type" "application/json"}
                 :body group}
        command {::cmr/request request
                 ::cmr/category :update}]
    (merge command (when options {:opts options}))))

(defn get-group-members
  [group-id & [opts]]
  (let [request {:method :get
                 :url (str "/access-control/groups/" group-id "/members")}
        request (if-let [pretty (get opts :pretty false)]
                  (assoc-in request [:query-params :pretty] pretty)
                  request)
        command {::cmr/request request
                 ::cmr/category :read}
        command-opts (dissoc opts :pretty)]
    (if (seq (keys command-opts))
      (assoc command :opts command-opts)
      command)))

(defn remove-group-members
  [group-id users & [options]]
  (let [request {:method :delete
                 :url (str "/access-control/groups/" group-id "/members")
                 :headers {"Content-Type" "application/json"}
                 :body users}
        command {::cmr/request request
                 ::cmr/category :read}]
    (merge command (when options (:opts command)))))

(defn get-acls
  "Return a query for requesting ACLs from "
  [& [query options]]
  (let [request {:method :get
                 :url "/access-control/acls"}
        request (merge request (when query {:query-params query}))
        command {::cmr/request request
                 ::cmr/category :read}]
    (merge command (when options {:opts options}))))

(defn create-acl
  "Return a query for requesting ACLs from "
  [acl & [options]]
  {::cmr/request
   {:method :post
    :url "/access-control/acls"
    :headers {"Content-Type" "application/json"}
    :body acl}
   ::cmr/category :create})

(defn get-permissions
  [query & [options]]
  (let [command {::cmr/request
                 {:method :get
                  :url (str "/access-control/permissions")
                  :query-params query}
                 ::cmr/category :read}]
    (merge command (when options {:opts options}))))

(defn get-s3-buckets
  [user-id & [providers options]]
  (let [query {:method :get
               :url "/access-control/s3-buckets"
               :query-params {:user-id user-id}}
        request (if (seq providers)
                  (assoc-in query [:query-params :provider] providers)
                  query)
        command {::cmr/request request
                 ::cmr/category :read}]
    (merge command (when options {:opts options}))))

(defn get-health
  "Returns a command to request the health-check endpoint."
  [& [options]]
   (let [{pretty? :pretty?} options
         request {:method :get
                  :url "/access-control/health"}
         query-params (when pretty?
                        {:query-params
                         {:pretty pretty?}})
         request (merge request query-params)]
     {::cmr/request request
      ::cmr/category :read}))
