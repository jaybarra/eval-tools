(ns eval.cmr.ingest-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest testing is]]
   [clojure.xml :as xml]
   [eval.cmr.ingest :as ingest]))

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
                 {:granule-ur "gr1"}
                 {:format :umm-json})]
    (is (= {:request
            {:method :post
             :headers {"Content-Type" "application/vnd.nasa.cmr.umm+json"}
             :url "/ingest/providers/FOO/validate/granule/null"}}
           (update command :request dissoc :body)))
    (is (= {:granule-ur "gr1"}
           (get-in command [:request :body]))))

  (let [command (ingest/validate-concept-metadata
                 :collection
                 "FOO"
                 collection-metadata-xml
                 {:format :xml})]
    (is (= {:request
            {:method :post
             :headers {"Content-Type" "application/xml"}
             :url "/ingest/providers/FOO/validate/collection/null"}}
           (update command :request dissoc :body)))
    (is (= (parse-xml collection-metadata-xml)
           (parse-xml (get-in command [:request :body]))))))

(deftest create-concept-test
  (testing "without giving a native-id"
    (let [command (ingest/create-concept
                   :collection
                   "FOO"
                   collection-metadata-xml
                   {:format :xml})]
      (is (= {:request
              {:method :put
               :url "/ingest/providers/FOO/collections"
               :headers {"Content-Type" "application/xml"}}}
             (update command :request dissoc :body)))
      (is (string? (get-in command [:request :body])))))

  (testing "without a native-id"
    (let [req (ingest/create-concept
               :collection
               "FOO"
               collection-metadata-xml
               {:format :json
                :native-id "foo123"})]
      (is (= {:request
              {:method :put
               :url "/ingest/providers/FOO/collections/foo123"
               :headers {"Content-Type" "application/json"}}}
             (update req :request dissoc :body))))))

(deftest delete-concept-test
  (is (= {:request
          {:method :delete
           :url "/ingest/providers/FOO/collections/1234"}}
         (ingest/delete-concept :collection "FOO" "1234"))))

(deftest create-association-test
  (is (= {:request
          {:method :put
           :url "/ingest/collections/c123/1/variables/v123"}}
         (ingest/create-association "c123" 1 "v123"))))
