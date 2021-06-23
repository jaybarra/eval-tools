(ns eval.cmr.acls-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :refer :all]
   [eval.cmr.acls :as acls]
   [jsonista.core :as json]))

(deftest get-groups-test
  (is (= {:url "/access-control/groups"
          :method :get}
         (acls/get-groups)))

  (testing "passing in a query"
    (is (= {:url "/access-control/groups"
            :method :get
            :query-params {:provider "FOO"}}
           (acls/get-groups {:provider "FOO"})))))

(deftest get-group-test
  (is (= {:url "/access-control/groups/foo-id"
          :method :get}
         (acls/get-group "foo-id")))

  (testing "passing in a query"
    (is (= {:url "/access-control/groups/foo-id"
            :method :get
            :query-params {:pretty true}}
           (acls/get-group "foo-id" {:pretty true }))))

  (testing "passing in a query"
    (is (= {:url "/access-control/groups/foo-id"
            :method :get}
           (acls/get-group "foo-id" {:pretty false})))))

(deftest create-group-test
  (let [{:keys [body] :as req} (acls/create-group
                                {:name "admins"
                                 :description "super duper users"
                                 :members ["user1" "user2"]})]
    (is (= {:method :post
            :url "/access-control/groups"
            :headers {"Content-Type" "application/json"}}
           (dissoc req :body)))
    (is (= {"name" "admins"
            "description" "super duper users"
            "members" ["user1" "user2"]}
           (json/read-value body)))))

(deftest delete-group-test
  (is (= {:url "/access-control/groups/foo-id"
          :method :delete}
         (acls/delete-group "foo-id"))))

(deftest update-group-test
  (let [{:keys [body] :as req} (acls/update-group "foo-id" {:description "a better description"})]
    (is (= {:url "/access-control/groups/foo-id"
            :method :put
            :headers {"Content-Type" "application/json"}}
           (dissoc req :body)))
    (is (= {"description" "a better description"}
           (json/read-value body)))))

(deftest get-group-members-test
  (is (= {:method :get
          :url "/access-control/groups/foo-group/members"}
         (acls/get-group-members "foo-group")))

  (testing "with params"
    (is (= {:method :get
            :url "/access-control/groups/foo-group/members"
            :query-params {:pretty true}}
           (acls/get-group-members "foo-group" {:pretty true})))))

(deftest remove-group-members-test
  (let [{:keys [body] :as req} (acls/remove-group-members "foo-group" ["user1" "user2"])]
    (is (= {:method :delete
            :url "/access-control/groups/foo-group/members"
            :headers {"Content-Type" "application/json"}}
           (dissoc req :body)))
    (is (= ["user1" "user2"]
           (json/read-value body)))))

(deftest get-acls-test
  (is (= {:url "/access-control/acls"
          :method :get}
         (acls/get-acls)))
  (testing "with query"
    (is (= {:url "/access-control/acls"
            :method :get
            :query-params {:provider "foo"}}
           (acls/get-acls {:provider "foo"})))))

(deftest create-acl-test
  (let [{:keys [body] :as req} (acls/create-acl
                                {:group_permissions
                                 [{:group_id "foo"
                                   :permissions ["read" "order"]}]
                                 :catalog_item_identity
                                 {:name "all granules"
                                  :provider_id "foo"
                                  :granule_applicable true}})]
    (is (= {:method :post
            :url "/access-control/acls"
            :headers {"Content-Type" "application/json"}}
           (dissoc req :body)))
    (is (= {"group_permissions"
            [{"group_id" "foo"
              "permissions" ["read" "order"]}]
            "catalog_item_identity"
            {"name" "all granules"
             "provider_id" "foo"
             "granule_applicable" true}}
           (json/read-value body)))))
