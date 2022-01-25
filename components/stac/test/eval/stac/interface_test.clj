(ns eval.stac.interface-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.stac.interface :as stac])
  (:import
   clojure.lang.ExceptionInfo))

(def catalog {:description "Root catalog for GES_DISC"
              :type "Catalog"
              :title "GES_DISC"
              :id "GES_DISC"
              :conformsTo ["https://api.stacspec.org/v1.0.0-beta.1/core"
                           "https://api.stacspec.org/v1.0.0-beta.1/item-search"
                           "https://api.stacspec.org/v1.0.0-beta.1/item-search#fields"
                           "https://api.stacspec.org/v1.0.0-beta.1/item-search#query"
                           "https://api.stacspec.org/v1.0.0-beta.1/item-search#sort"
                           "https://api.stacspec.org/v1.0.0-beta.1/item-search#context"
                           "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core"
                           "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30"
                           "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson"]
              :stac_version "1.0.0"
              :links [{:rel "self"
                       :type "application/json"
                       :title "Provider catalog"
                       :href "https://mock.test/stac/GES_DISC"}
                      {:rel "root"
                       :type "application/json"
                       :title "Root catalog"
                       :href "https://mock.test/stac/"}
                      {:rel "collections"
                       :type "application/json"
                       :title "Provider Collections"
                       :href "https://mock.test/stac/GES_DISC/collections"}
                      {:rel "search"
                       :method "GET"
                       :type "application/geo+json"
                       :title "Provider Item Search"
                       :href "https://mock.test/stac/GES_DISC/search"}
                      {:rel "search"
                       :method "POST"
                       :type "application/geo+json"
                       :title "Provider Item Search"
                       :href "https://mock.test/stac/GES_DISC/search"}
                      {:rel "conformance"
                       :type "application/geo+json"
                       :title "Conformance Classes"
                       :href "https://mock.test/stac/GES_DISC/conformance"}
                      {:rel "service-desc"
                       :type "application/vnd.oai.openapi;version=3.0"
                       :title "OpenAPI Doc"
                       :href "https://api.stacspec.org/v1.0.0-beta.1/openapi.yaml"}
                      {:rel "service-doc"
                       :type "text/html"
                       :title "HTML documentation"
                       :href "https://api.stacspec.org/v1.0.0-beta.1/index.html"}
                      {:rel "child"
                       :type "application/json"
                       :href "https://mock.test/stac/GES_DISC/collections/ACOS_L2_Lite_FP.v7.3"}
                      {:rel "next"
                       :href "https://mock.test/stac/GES_DISC?page=2"}]})

(deftest children--valid-catalog--children-returned
  (is (= [{:rel "child"
           :type "application/json"
           :href
           "https://mock.test/stac/GES_DISC/collections/ACOS_L2_Lite_FP.v7.3"}]
         (stac/children catalog))))

(deftest validate--valid--self-returned
  (is (= catalog (stac/validate catalog))))

(deftest validate--invalid--exception-thrown
  (let [input {}]
    (is (thrown-with-msg?
         ExceptionInfo
         #"Catalog does not match STAC schema"
         (stac/validate input)))

    (try
      (stac/validate input)
      (catch Exception ex
        (let [data (ex-data ex)]
          (is (= #{:errors :catalog}
                 (-> data keys set))
              "Useful keys are in the result")
          
          (is (= input (:catalog data))
              "The invalid catalog is returned with the errors.")
          
          (is (coll? (:errors data))
              "The list of specific errors is returned"))))))
