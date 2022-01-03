(ns eval.cmr.commands.acls-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [eval.cmr.commands.acls :as acls]
   [eval.cmr.client :as cmr]))

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

(deftest create-group--valid-query
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

(deftest remove-group-members
  (testing "When removing group members from a group"
    (let [users ["user1" "user2"]
          group-id "foo-group"
          command (acls/remove-group-members group-id users)]
      (is (= {:method :delete
              :url "/access-control/groups/foo-group/members"
              :headers {"Content-Type" "application/json"}
              :body users}
             (::cmr/request command))
          "Then the command is correctly generated"))
    
    (testing "with options"
      (let [command (acls/remove-group-members "foo-group" ["user1" "user2"] {:x :y})]
        (is (= {:method :delete
                :url "/access-control/groups/foo-group/members"
                :headers {"Content-Type" "application/json"}}
               (::cmr/request (update command ::cmr/request dissoc :body))))))))

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

(deftest get-s3-buckets
  (testing "Given a user-id"
    (let [user "test-user"]
      (is (= {:method :get
              :url "/access-control/s3-buckets"
              :query-params {:user-id user}}
             (::cmr/request (acls/get-s3-buckets user)))
          "Then the request is for that user.")
      
      (testing "When passing list of providers and user"
        (let [providers ["PROV1" "PROV2"]]
          (is (= {:method :get
                  :url "/access-control/s3-buckets"
                  :query-params {:user-id user
                                 :provider providers}}
                 (::cmr/request (acls/get-s3-buckets user providers)))
              "Then the request has that user and providers specified")))
      
      (testing "When passing in options"
        (let [user "user3"
              providers ["PROV2" "PROV3"]
              options {:anonymous false}]
          (is (= {:method :get
                  :url "/access-control/s3-buckets"
                  :query-params {:user-id user
                                 :provider providers}}
                 (::cmr/request (acls/get-s3-buckets user providers options)))))))))

(deftest get-health--request-variations
  (testing "with no options"
    (is (= {:method :get
            :url "/access-control/health"}
           (::cmr/request (acls/get-health)))
        "Then the request has no query-params"))
  
  (testing "with pretty formatting option"
    (let [options {:pretty? true}]
      (is (= {:method :get
              :url "/access-control/health"
              :query-params {:pretty true}}
             (::cmr/request (acls/get-health options)))
          "Then the pretty query-param is set"))))
