(ns eval.cmr.ingest-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest testing is are]]
   [clojure.xml :as xml]
   [eval.cmr.ingest :as ingest]
   [jsonista.core :as json]))

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
  [content]
  (-> content .getBytes io/input-stream xml/parse))

(deftest validate-concept-metadata-test
  (let [{:keys [body] :as req} (ingest/validate-concept-metadata
                                :granule
                                "FOO"
                                {:granule-ur "gr1"}
                                {:format :umm-json})]
    (is (= {:method :post
            :headers {"Content-Type" "application/vnd.nasa.cmr.umm+json"}
            :url "/ingest/providers/FOO/validate/granule/null"}
           (dissoc req :body)))
    (is (= {"granule-ur" "gr1"}
           (json/read-value body))))

  (let [{:keys [body] :as req} (ingest/validate-concept-metadata
                                :collection
                                "FOO"
                                collection-metadata-xml
                                {:format :xml})]
    (is (= {:method :post
            :headers {"Content-Type" "application/xml"}
            :url "/ingest/providers/FOO/validate/collection/null"}
           (dissoc req :body)))
    (is (= (parse-xml collection-metadata-xml)
           (parse-xml body)))))

(deftest create-concept-test
  (let [{:keys [body] :as req} (ingest/create-concept :collection "FOO" {})]
    (is (= {:method :put
            :url "/ingest/providers/FOO/collections"}
           (dissoc req :body)))
    (is (= {} (json/read-value body)))))

(deftest delete-concept-test
  (is (= {:method :delete
          :url "/ingest/providers/FOO/collections/1234"}
         (ingest/delete-concept :collection "FOO" "1234"))))

(deftest create-association-test
  (is (= {:method :put
          :url "/ingest/collections/c123/1/variables/v123"}
         (ingest/create-association "c123" 1 "v123"))))

#_(deftest reindex-all-collections-test
    (is (= {:method :post
            :url "/ingest/jobs/reindex-all-collections"}
           (ingest/reindex-all-collections))))

#_(deftest reindex-permitted-groups-test
    (is (= {:method :post
            :url "/ingest/jobs/reindex-permitted-groups"}
           (ingest/reindex-permitted-groups ["g1234"]))))
