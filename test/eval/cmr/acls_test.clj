(ns eval.cmr.acls-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :refer :all]
   [eval.cmr.acls :as acls]
   [jsonista.core :as json]))

(deftest get-groups-test
  (is (= {:request {:url "/access-control/groups"
                    :method :get}}
         (acls/get-groups)))

  (testing "passing in a query"
    (is (= {:request {:url "/access-control/groups"
                      :method :get
                      :query-params {:provider "FOO"}}}
           (acls/get-groups {:provider "FOO"})))))

(deftest get-group-test
  (is (= {:request {:url "/access-control/groups/foo-id"
                    :method :get}}
         (acls/get-group "foo-id")))

  (testing "passing in a query true"
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :get
                      :query-params {:pretty true}}}
           (acls/get-group "foo-id" {:pretty true}))))

  (testing "passing in a query false"
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :get}}
           (acls/get-group "foo-id" {:pretty false})))))

(deftest create-group-test
  (let [command (acls/create-group
                 {:name "admins"
                  :description "super duper users"
                  :members ["user1" "user2"]})]
    (is (= {:request {:method :post
                      :url "/access-control/groups"
                      :headers {"Content-Type" "application/json"}}}
           (update command :request dissoc :body)))
    (is (= {"name" "admins"
            "description" "super duper users"
            "members" ["user1" "user2"]}
           (json/read-value (get-in command [:request :body]))))))

(deftest delete-group-test
  (is (= {:request {:url "/access-control/groups/foo-id"
                    :method :delete}}
         (acls/delete-group "foo-id"))))

(deftest update-group-test
  (let [command (acls/update-group "foo-id" {:description "a better description"})]
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :put
                      :headers {"Content-Type" "application/json"}}}
           (update command :request dissoc :body)))
    (is (= {"description" "a better description"}
           (json/read-value (get-in command [:request :body]))))))

(deftest get-group-members-test
  (is (= {:request {:method :get
                    :url "/access-control/groups/foo-group/members"}}
         (acls/get-group-members "foo-group")))

  (testing "with params"
    (is (= {:request {:method :get
                      :url "/access-control/groups/foo-group/members"
                      :query-params {:pretty true}}}
           (acls/get-group-members "foo-group" {:pretty true})))))

(deftest remove-group-members-test
  (let [command (acls/remove-group-members "foo-group" ["user1" "user2"])]
    (is (= {:request {:method :delete
                      :url "/access-control/groups/foo-group/members"
                      :headers {"Content-Type" "application/json"}}}
           (update command :request dissoc :body)))
    (is (= ["user1" "user2"]
           (json/read-value (get-in command [:request :body]))))))

(deftest get-acls-test
  (is (= {:request {:url "/access-control/acls"
                    :method :get}}
         (acls/get-acls)))
  (testing "with query"
    (is (= {:request {:url "/access-control/acls"
                      :method :get
                      :query-params {:provider "foo"}}}
           (acls/get-acls {:provider "foo"})))))

(deftest create-acl-test
  (let [command (acls/create-acl
                 {:group_permissions
                  [{:group_id "foo"
                    :permissions ["read" "order"]}]
                  :catalog_item_identity
                  {:name "all granules"
                   :provider_id "foo"
                   :granule_applicable true}})]
    (is (= {:request {:method :post
                      :url "/access-control/acls"
                      :headers {"Content-Type" "application/json"}}}
           (update command :request dissoc :body)))
    (is (= {"group_permissions"
            [{"group_id" "foo"
              "permissions" ["read" "order"]}]
            "catalog_item_identity"
            {"name" "all granules"
             "provider_id" "foo"
             "granule_applicable" true}}
           (json/read-value (get-in command [:request :body]))))))
