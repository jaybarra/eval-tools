(ns eval.cmr.commands.acls-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.commands.acls :as acls]
   [eval.cmr.core :as cmr]))

(deftest get-groups-test
  (let [command (acls/get-groups)]
    (is (spec/valid? ::cmr/command (acls/get-groups)))
    (is (= {:url "/access-control/groups"
            :method :get}
           (::cmr/request command))))

  (testing "passing in a query"
    (let [command (acls/get-groups {:provider "FOO"})]
      (is (spec/valid? ::cmr/command command))
      (is (= {:provider "FOO"} (get-in command [::cmr/request :query-params])))))

  (testing "passing in opts"
    (let [command (acls/get-groups {:provider "FOO"}
                                   {:anonymous true})]
      (is (= {:url "/access-control/groups"
              :method :get
              :query-params {:provider "FOO"}}
             (::cmr/request command)))
      (is (= {:anonymous true}
             (:opts command))))))

(deftest get-group-test
  (testing "route is correct"
    (let [command (acls/get-group "foo-id")]
      (is (spec/valid? ::cmr/command command))

      (is (= "/access-control/groups/foo-id" (get-in command [::cmr/request :url])))
      (is (= :get (get-in command [::cmr/request :method])))))

  (testing "passing in a query"
    (let [command (acls/get-group "foo-id" {:pretty true})]
      (is (spec/valid? ::cmr/command command))

      (is (= {:pretty true} (get-in command [::cmr/request :query-params]))))))

(deftest create-group-test
  (let [test-group {:name "admins"
                    :description "super duper users"
                    :members ["user1" "user2"]}
        command (acls/create-group test-group)]
    (is (spec/valid? ::cmr/command command))
    (is (= test-group (get-in command [::cmr/request :body]))))

  (testing "with additional options"
    (let [test-group {:name "admins"
                      :description "super duper users"
                      :members ["user1" "user2"]}
          command (acls/create-group
                   test-group
                   {:anonymous false})]
      (is (= {:method :post
              :url "/access-control/groups"
              :headers {"Content-Type" "application/json"}
              :body test-group}
              (::cmr/request command))))))

(deftest delete-group-test
  (is (spec/valid? ::cmr/command (acls/delete-group "foo")))

  (is (= {:url "/access-control/groups/foo-id"
          :method :delete}
         (::cmr/request (acls/delete-group "foo-id"))))
  (testing "with options"
    (is (= {:url "/access-control/groups/foo-id"
            :method :delete}
           (::cmr/request (acls/delete-group "foo-id" {:foo :bar}))))))

(deftest update-group-test
  (let [command (acls/update-group "foo-id" {:description "a better description"})]
    (is (= {:url "/access-control/groups/foo-id"
            :method :put
            :headers {"Content-Type" "application/json"}
            :body {:description "a better description"}}
           (::cmr/request command))))

  (testing "with options"
    (let [command (acls/update-group "foo-id" {:description "a worse description"} {:anonymous false})]
      (is (= {:url "/access-control/groups/foo-id"
              :method :put
              :headers {"Content-Type" "application/json"}
              :body {:description "a worse description"}}
             (::cmr/request command)))
      (is (= {:anonymous false}
             (:opts command))))))

(deftest get-group-members-test
  (is (= {:method :get
          :url "/access-control/groups/foo-group/members"}
         (::cmr/request (acls/get-group-members "foo-group"))))

  (testing "with options"
    (is (= {:method :get
            :url "/access-control/groups/foo-group/members"}
           (::cmr/request (acls/get-group-members "foo-group" {:anonymous true})))))

  (testing "with params"
    (is (= {:method :get
            :url "/access-control/groups/foo-group/members"
            :query-params {:pretty true}}
           (::cmr/request (acls/get-group-members "foo-group" {:pretty true}))))))

(deftest remove-group-members-test
  (let [command (acls/remove-group-members "foo-group" ["user1" "user2"])]
    (is (=  {:method :delete
             :url "/access-control/groups/foo-group/members"
             :headers {"Content-Type" "application/json"}
             :body ["user1" "user2"]}
            (::cmr/request command))))
  (testing "with options"
    (let [command (acls/remove-group-members "foo-group" ["user1" "user2"] {:x :y})]
      (is (= {:method :delete
              :url "/access-control/groups/foo-group/members"
              :headers {"Content-Type" "application/json"}}
             (::cmr/request (update command ::cmr/request dissoc :body)))))))

(deftest get-acls-test
  (is (= {:url "/access-control/acls"
           :method :get}
          (::cmr/request (acls/get-acls))))
  (testing "with query"
    (is (= {:url "/access-control/acls"
             :method :get
             :query-params {:provider "foo"}}
           (::cmr/request (acls/get-acls {:provider "foo"})))))

  (testing "with opts"
    (is (= {:url "/access-control/acls"
            :method :get
            :query-params {:provider "foo"}}
           (::cmr/request (acls/get-acls {:provider "foo"} {:anonymous? true}))))))

(deftest create-acl-test
  (let [acl {:group_permissions
             [{:group_id "foo"
               :permissions ["read" "order"]}]
             :catalog_item_identity
             {:name "all granules"
              :provider_id "foo"
              :granule_applicable true}}
        command (acls/create-acl acl)]
    (is (= {:method :post
            :url "/access-control/acls"
            :headers {"Content-Type" "application/json"}
            :body acl}
           (::cmr/request command))))

  (testing "passing options"
    (let [acl {:group_permissions
               [{:group_id "foo"
                 :permissions ["read" "order"]}]
               :catalog_item_identity
               {:name "all granules"
                :provider_id "foo"
                :granule_applicable true}}
          command (acls/create-acl acl {:foo :bar})]
      (is (= {:method :post
              :url "/access-control/acls"
              :headers {"Content-Type" "application/json"}
              :body acl}
              (::cmr/request command))))))

(deftest get-permissions-test
  (is (= {:method :get
           :url "/access-control/permissions"
           :query-params {:group-id "GR123"}}
          (::cmr/request (acls/get-permissions {:group-id "GR123"}))))
  (testing "with options"
    (is (= {:method :get
            :url "/access-control/permissions"
            :query-params {:group-id "GR123"}}
           (::cmr/request (acls/get-permissions {:group-id "GR123"} {:baz :bat}))))))

(deftest get-s3-buckets-test
  (is (= {:method :get
           :url "/access-control/s3-buckets"
           :query-params {:user-id "user1"}}
         (::cmr/request (acls/get-s3-buckets "user1"))))
  (testing "passing list of providers"
    (is (= {:method :get
             :url "/access-control/s3-buckets"
             :query-params {:user-id "user2"
                            :provider ["PROV1" "PROV2"]}}
            (::cmr/request (acls/get-s3-buckets "user2" ["PROV1" "PROV2"])))))
  (testing "passing options"
    (is (= {:method :get
            :url "/access-control/s3-buckets"
            :query-params {:user-id "user3"
                           :provider ["PROV2" "PROV3"]}}
           (::cmr/request (acls/get-s3-buckets "user3" ["PROV2" "PROV3"] {:anonymous false}))))))

(deftest get-health-test
  (is (= {:method :get
          :url "/access-control/health"}
          (::cmr/request (acls/get-health))))
  (is (= {:method :get
          :url "/access-control/health"
          :query-params {:pretty true}}
         (::cmr/request (acls/get-health {:pretty true})))))
