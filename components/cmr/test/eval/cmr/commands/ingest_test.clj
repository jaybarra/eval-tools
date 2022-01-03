(ns eval.cmr.commands.ingest-test
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing is]]
   [clojure.xml :as xml]
   [eval.cmr.commands.ingest :as ingest]
   [eval.cmr.client :as cmr]))

(def collection-metadata-xml
  "<Collection>
    <ShortName>ShortName_Larc</ShortName>
    <VersionId>Version01</VersionId>
    <InsertTime>1999-12-31T19:00:00-05:00</InsertTime>
    <LastUpdate>1999-12-31T19:00:00-05:00</LastUpdate>
    <DeleteTime>2015-05-23T22:30:59</DeleteTime>
    <LongName>LarcLongName</LongName>
    <DataSetId>LarcDatasetId</DataSetId>
    <Description>A minimal valid collection</Description>
    <Orderable>true</Orderable>
    <Visible>true</Visible>
  </Collection>")

(defn parse-xml
  [^String content]
  (-> content .getBytes io/input-stream xml/parse))

(deftest validate-concept-metadata-test
  (let [command (ingest/validate-concept-metadata
                 :granule
                 "FOO"
                 "my-native-id"
                 {:granule-ur "gr1"}
                 {:format :umm-json})]
    (is (spec/valid? ::cmr/command command))
    (is (= {:method :post
            :headers {:content-type "application/vnd.nasa.cmr.umm+json"}
            :url "/ingest/providers/FOO/validate/granule/my-native-id"
            :body {:granule-ur "gr1"}}
           (::cmr/request command))))

  (let [command (ingest/validate-concept-metadata
                 :collection
                 "FOO"
                 "my-native-id"
                 collection-metadata-xml
                 {:format :xml})]
    (is (= {:method :post
            :headers {:content-type "application/xml"}
            :url "/ingest/providers/FOO/validate/collection/my-native-id"
            :body collection-metadata-xml}
           (::cmr/request command)))
    (is (= (parse-xml collection-metadata-xml)
           (parse-xml (get-in command [::cmr/request :body]))))))

(deftest create-concept-test
  (testing "without giving a native-id"
    (let [command (ingest/create-concept
                   :collection
                   "FOO"
                   collection-metadata-xml
                   {:format :xml})]
      (is (spec/valid? ::cmr/command command))
      (is (= {:method :put
              :url "/ingest/providers/FOO/collections"
              :headers {:content-type "application/xml"}}
             (::cmr/request (update command ::cmr/request dissoc :body))))
      (is (string? (get-in command [::cmr/request :body])))))

  (testing "without a native-id"
    (let [command (ingest/create-concept
               :collection
               "FOO"
               collection-metadata-xml
               {:format :json
                :native-id "foo123"})]
      (is (= {:method :put
              :url "/ingest/providers/FOO/collections/foo123"
              :headers {:content-type "application/json"}
              :body collection-metadata-xml}
             (::cmr/request command))))))

(deftest delete-concept-test
  (is (spec/valid? ::cmr/command (ingest/delete-concept :collection "foo" "abc")))
  (is (= {:method :delete
          :url "/ingest/providers/FOO/collections/1234"}
         (::cmr/request (ingest/delete-concept :collection "FOO" "1234")))))

(deftest create-association-test
  (is (spec/valid? ::cmr/command (ingest/create-association "c123" 2 "v456")))
  (is (= {:method :put
          :url "/ingest/collections/c123/1/variables/v123"}
         (::cmr/request (ingest/create-association "c123" 1 "v123")))))
