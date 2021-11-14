(ns eval.services.cmr.bulk.granule-test
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest is]]
   [eval.cmr.commands.bulk-granule :as bg]
   [eval.cmr.core :as cmr]
   [eval.services.cmr.bulk.granule :as bulk-granule]
   [jsonista.core :as json]))

(deftest submit-job-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ command]
                   (let [job-def (get-in command [::cmr/request :body])]
                     (is (= :post (get-in command [::cmr/request :method])))
                     (is (= "/ingest/providers/FOO_PROV/bulk-update/granules" (get-in command [::cmr/request :url])))
                     (is (spec/valid? ::bg/job job-def)
                         (spec/explain-str ::bg/job job-def))
                     {:status 200
                      :headers {"Content-Type" "application/json"}
                      :body (json/write-value-as-string {:task-id 1 :status 200})}))
                 (-token [_] "mock-token"))
        job {::bg/operation "UPDATE_FIELD"
             ::bg/update-field "S3Link"
             ::bg/updates ["foo-granule-1" "s3://example/foo1"]}]
    (bulk-granule/submit-job! client "FOO_PROV" job)))

(deftest fetch-job-status-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ command]
                   (is (= :get (get-in command [::cmr/request :method])))
                   (is (= "/ingest/granule-bulk-update/status/12345" (get-in command [::cmr/request :url]))))
                 (-token [_] "mock-token"))]
    (bulk-granule/fetch-job-status client 12345)))

(deftest trigger-status-update-test
  (let [client (reify cmr/CmrClient
                 (-invoke [_ command]
                   (is (= :post (get-in command [::cmr/request :method])))
                   (is (= "/ingest/granule-bulk-update/status" (get-in command [::cmr/request :url]))))
                 (-token [_] "mock-token"))]
    (bulk-granule/trigger-status-update! client)))
