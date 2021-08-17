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
   [clojure.spec.alpha :as spec]))

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
  [& [query opts]]
  (let [request {:method :get
                 :url "/access-control/groups"}
        request (if query (assoc request :query-params query) request)
        command {:request request}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn create-group
  [group & [opts]]
  (let [command {:request {:method :post
                           :url "/access-control/groups"
                           :headers {"Content-Type" "application/json"}
                           :body group}}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn get-group
  "Constructs a query for getting a group by id.

  Options
  pretty | boolean"
  [group-id & [opts]]
  (let [base {:method :get
              :url (str "/access-control/groups/" group-id)}
        req (if-let [pretty (get opts :pretty)]
              (assoc-in base [:query-params :pretty] pretty)
              base)
        command {:request req}
        command-opts (dissoc opts :pretty)]
    (if (seq (keys command-opts))
      (assoc command :opts command-opts)
      command)))

(defn delete-group
  "Constructs a query to delete a group by id."
  [group-id & [opts]]
  (let [command {:request {:method :delete
                           :url (str "/access-control/groups/" group-id)}}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn update-group
  "Constructs a query to update a group."
  [group-id group & [opts]]
  (let [request {:method :put
                 :url (str "/access-control/groups/" group-id)
                 :headers {"Content-Type" "application/json"}
                 :body group}
        command {:request request}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn get-group-members
  [group-id & [opts]]
  (let [request {:method :get
                 :url (str "/access-control/groups/" group-id "/members")}
        request (if-let [pretty (get opts :pretty false)]
                  (assoc-in request [:query-params :pretty] pretty)
                  request)
        command {:request request}
        command-opts (dissoc opts :pretty)]
    (if (seq (keys command-opts))
      (assoc command :opts command-opts)
      command)))

(defn remove-group-members
  [group-id users & [opts]]
  (let [command
        {:request {:method :delete
                   :url (str "/access-control/groups/" group-id "/members")
                   :headers {"Content-Type" "application/json"}
                   :body users}}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn get-acls
  "Return a query for requesting ACLs from "
  [& [query opts]]
  (let [request {:method :get
                 :url "/access-control/acls"}
        request (if query
                  (assoc request :query-params query)
                  request)
        command {:request request}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn create-acl
  "Return a query for requesting ACLs from "
  [acl & [opts]]
  (let [command
        {:request
         {:method :post
          :url "/access-control/acls"
          :headers {"Content-Type" "application/json"}
          :body acl}}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn get-permissions
  [query & [opts]]
  (let [command
        {:request
         {:method :get
          :url (str "/access-control/permissions")
          :query-params query}}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn get-s3-buckets
  [user-id & [providers opts]]
  (let [query {:method :get
               :url "/access-control/s3-buckets"
               :query-params {:user-id user-id}}
        request (if providers
                  (assoc-in query [:query-params :provider] providers)
                  query)
        command {:request request}]
    (if opts
      (assoc command :opts opts)
      command)))

(defn get-health
  [& [opts]]
  (let [req {:method :get
             :url "/access-control/health"}]
    {:request (if-let [pretty (get opts :pretty)]
                (assoc-in req [:query-params :pretty] pretty)
                req)}))
