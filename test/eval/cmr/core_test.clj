(ns eval.cmr.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.spec.alpha :as spec]
   [eval.cmr.core :as cmr :refer [state->cmr]]
   [muuntaja.core :as muuntaja]))

(def state {:connections
            {::cmr/cmr {::cmr/env :local
                        ::cmr/url "http://localhost:3000"}}})

(deftest cmr-conn-test
  (are [env out] (= out (cmr/cmr env))
    :prod {::cmr/env :prod
           ::cmr/url "https://cmr.earthdata.nasa.gov"}

    :uat {::cmr/env :uat
          ::cmr/url "https://cmr.uat.earthdata.nasa.gov"}

    :sit {::cmr/env :sit
          ::cmr/url "https://cmr.sit.earthdata.nasa.gov"}

    :local {::cmr/env :local
            ::cmr/url "http://localhost:3003"}

    :foo {::cmr/env :local
          ::cmr/url "http://localhost:3003"}))

(deftest state->cmr-opts-test
  (let [result (state->cmr state)]
    (is (spec/valid? ::cmr/cmr result)
        (str (spec/explain-data ::cmr/cmr result)))

    (testing "permutations"
      (are [input]
          (= false (spec/valid? ::cmr/cmr input))

        ;; missing
        {}

        ;; invalid env
        {::cmr/env :sat
         ::cmr/url "http://localhost:3000"}

        ;; invalid url
        {::cmr/env :prod
         ::cmr/url "http://cmr.not.earthdata.nasa.goov"}

        ;; invalid namespace
        {:env :prod
         :url "https://cmr.earthdata.nasa.gov"}))))

(def facet-group
  {:title "Browse Granules"
   :type "group"
   :has_children true
   :children
   [{:title "Temporal"
     :type "group"
     :applied false
     :has_children true
     :children
     [{:title "Year"
       :type "group"
       :applied false
       :has_children true
       :children
       [{:title "2016"
         :type "filter"
         :applied false
         :count 1
         :links
         {:apply
          "https://search/granules.json?collection_concept_id=TEST"}
         :has_children true}]}]}]})

(deftest v2-facets-specs
  (is (= true (spec/valid? :v2-facets/facets facet-group)))
  (is (= false (spec/valid? :v2-facets/facets (dissoc facet-group :title)))))

(deftest facets-contains-type-test  
  (is (= true (cmr/facets-contains-type? "Temporal" facet-group)))
  (is (= false (cmr/facets-contains-type? "Spatial" facet-group))))
