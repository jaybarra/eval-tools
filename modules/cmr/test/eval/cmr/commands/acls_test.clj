(ns eval.cmr.commands.acls-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.commands.acls :as acls]
   [eval.cmr.core :as cmr]))

(deftest get-groups-test
  (is (spec/valid? ::cmr/command (acls/get-groups)))

  (is (= {:request {:url "/access-control/groups"
                    :method :get}}
         (acls/get-groups)))

  (testing "passing in a query"
    (is (= {:request {:url "/access-control/groups"
                      :method :get
                      :query-params {:provider "FOO"}}}
           (acls/get-groups {:provider "FOO"}))))

  (testing "passing in opts"
    (is (= {:request {:url "/access-control/groups"
                      :method :get
                      :query-params {:provider "FOO"}}
            :opts {:anonymous true}}
           (acls/get-groups {:provider "FOO"}
                            {:anonymous true})))))

(deftest get-group-test
  (is (spec/valid? ::cmr/command (acls/get-group "foo")))

  (is (= {:request {:url "/access-control/groups/foo-id"
                    :method :get}}
         (acls/get-group "foo-id")))

  (testing "passing in a query"
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :get
                      :query-params {:pretty true}}}
           (acls/get-group "foo-id" {:pretty true})))
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :get}}
           (acls/get-group "foo-id" {:pretty false}))))

  (testing "passing in opts"
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :get}
            :opts {:token "FLOO"}}
           (acls/get-group "foo-id" {:pretty false
                                     :token "FLOO"})))))

(deftest create-group-test
  (let [command (acls/create-group
                 {:name "admins"
                  :description "super duper users"
                  :members ["user1" "user2"]})]
    (is (spec/valid? ::cmr/command command))

    (is (= {:request {:method :post
                      :url "/access-control/groups"
                      :headers {"Content-Type" "application/json"}}}
           (update command :request dissoc :body)))
    (is (= {:name "admins"
            :description "super duper users"
            :members ["user1" "user2"]}
           (get-in command [:request :body]))))

  (testing "with additional options"
    (let [command (acls/create-group
                   {:name "admins"
                    :description "super duper users"
                    :members ["user1" "user2"]}
                   {:anonymous false})]
      (is (= {:request {:method :post
                        :url "/access-control/groups"
                        :headers {"Content-Type" "application/json"}}
              :opts {:anonymous false}}
             (update command :request dissoc :body))))))

(deftest delete-group-test
  (is (spec/valid? ::cmr/command (acls/delete-group "foo")))

  (is (= {:request {:url "/access-control/groups/foo-id"
                    :method :delete}}
         (acls/delete-group "foo-id")))
  (testing "with options"
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :delete}
            :opts {:foo :bar}}
           (acls/delete-group "foo-id" {:foo :bar})))))

(deftest update-group-test
  (let [command (acls/update-group "foo-id" {:description "a better description"})]
    (is (= {:request {:url "/access-control/groups/foo-id"
                      :method :put
                      :headers {"Content-Type" "application/json"}}}
           (update command :request dissoc :body)))
    (is (= {:description "a better description"}
           (get-in command [:request :body]))))

  (testing "with options"
    (let [command (acls/update-group "foo-id" {:description "a worse description"} {:foo :buz})]
      (is (= {:request {:url "/access-control/groups/foo-id"
                        :method :put
                        :headers {"Content-Type" "application/json"}}
              :opts {:foo :buz}}
             (update command :request dissoc :body))))))

(deftest get-group-members-test
  (is (= {:request {:method :get
                    :url "/access-control/groups/foo-group/members"}}
         (acls/get-group-members "foo-group")))

  (testing "with options"
    (is (= {:request {:method :get
                      :url "/access-control/groups/foo-group/members"}
            :opts {:bar :baz}}
           (acls/get-group-members "foo-group" {:bar :baz}))))

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
           (get-in command [:request :body]))))
  (testing "with options"
    (let [command (acls/remove-group-members "foo-group" ["user1" "user2"] {:x :y})]
      (is (= {:request {:method :delete
                        :url "/access-control/groups/foo-group/members"
                        :headers {"Content-Type" "application/json"}}
              :opts {:x :y}}
             (update command :request dissoc :body))))))

(deftest get-acls-test
  (is (= {:request {:url "/access-control/acls"
                    :method :get}}
         (acls/get-acls)))
  (testing "with query"
    (is (= {:request {:url "/access-control/acls"
                      :method :get
                      :query-params {:provider "foo"}}}
           (acls/get-acls {:provider "foo"}))))

  (testing "with opts"
    (is (= {:request {:url "/access-control/acls"
                      :method :get
                      :query-params {:provider "foo"}}
            :opts {:anonymous? true}}
           (acls/get-acls {:provider "foo"} {:anonymous? true})))))

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
    (is (= {:group_permissions
            [{:group_id "foo"
              :permissions ["read" "order"]}]
            :catalog_item_identity
            {:name "all granules"
             :provider_id "foo"
             :granule_applicable true}}
           (get-in command [:request :body]))))

  (testing "passing options"
    (let [command (acls/create-acl
                   {:group_permissions
                    [{:group_id "foo"
                      :permissions ["read" "order"]}]
                    :catalog_item_identity
                    {:name "all granules"
                     :provider_id "foo"
                     :granule_applicable true}}
                   {:foo :bar})]
      (is (= {:request {:method :post
                        :url "/access-control/acls"
                        :headers {"Content-Type" "application/json"}}
              :opts {:foo :bar}}
             (update command :request dissoc :body))))))

(deftest get-permissions-test
  (is (= {:request {:method :get
                    :url "/access-control/permissions"
                    :query-params {:group-id "GR123"}}}
         (acls/get-permissions {:group-id "GR123"})))
  (testing "with options"
    (is (= {:request {:method :get
                      :url "/access-control/permissions"
                      :query-params {:group-id "GR123"}}
            :opts {:baz :bat}}
           (acls/get-permissions {:group-id "GR123"} {:baz :bat})))))

(deftest get-s3-buckets-test
  (is (= {:request {:method :get
                    :url "/access-control/s3-buckets"
                    :query-params {:user-id "user1"}}}
         (acls/get-s3-buckets "user1")))
  (testing "passing list of providers"
    (is (= {:request {:method :get
                      :url "/access-control/s3-buckets"
                      :query-params {:user-id "user2"
                                     :provider ["PROV1" "PROV2"]}}}
           (acls/get-s3-buckets "user2" ["PROV1" "PROV2"]))))
  (testing "passing options"
    (is (= {:request {:method :get
                      :url "/access-control/s3-buckets"
                      :query-params {:user-id "user3"
                                     :provider ["PROV2" "PROV3"]}}
            :opts {:anonymous false}}
           (acls/get-s3-buckets "user3" ["PROV2" "PROV3"] {:anonymous false})))))

(deftest get-health-test
  (is (= {:request {:method :get
                    :url "/access-control/health"}}
         (acls/get-health)))
  (is (= {:request {:method :get
                    :url "/access-control/health"
                    :query-params {:pretty true}}}
         (acls/get-health {:pretty true}))))
