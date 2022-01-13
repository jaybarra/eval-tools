(ns eval.cmr.interface.acls
  "Namespace for interacting with the ACLs, groups, and permissions. 
  Defines commands and queries for use with the [[eval.cmr.interface/invoke]] function.

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
   [eval.cmr.commands.acls :as acls]))

(defn get-groups
  [& args]
  (acls/get-groups args))

(defn create-group
  [group & args]
  (acls/create-group group args))

(defn get-group
  "Constructs a query for getting a group by id."
  [group-id]
  (acls/get-group group-id))

(defn delete-group
  "Constructs a query to delete a group by id."
  [group-id]
  (acls/delete-group group-id))

(defn update-group
  "Constructs a query to update a group."
  [group-id group]
  (acls/update-group group-id group))

(defn get-group-members
  [group-id]
  (acls/get-group-members group-id))

(defn remove-group-members
  [group-id users]
  (acls/remove-group-members group-id users))

(defn get-acls
  "Return a query for requesting ACLs from "
  [query]
  (acls/get-acls query))

(defn create-acl
  "Return a query for requesting ACLs from "
  [acl]
  (acls/create-acl acl))

(defn get-permissions
  [query]
  (acls/get-permissions query))

(defn get-s3-buckets
  [user-id providers]
  (acls/get-s3-buckets user-id providers))

(defn get-health
  [options]
  (acls/get-health options))
