(ns eval.stac.core
  (:require
   [clj-http.client :as client]
   [clojure.java.io :as io]
   [jsonista.core :as json]
   [scjsv.core :as v]))

(def validate (v/validator (slurp (io/resource "stac_catalog_schema_1.0.0.json"))))

(defn validate-is-stac?
  [catalog]
  (let [validations (validate catalog)
        errors  (filter #(= "error" (:level %)) validations)]
    (when (seq errors)
     (throw (ex-info "Catalog does not match STAC schema." {:errors errors})))
    catalog))

(defn get-catalog
  [url]
  (let [response (client/get url {:accept :json})]
    (-> response
        :body
        (json/read-value json/keyword-keys-object-mapper)
        validate-is-stac?)))

(defn children
  [catalog]
  (let [links (:links catalog)]
    (filter #(= "child" (:rel %)) links)))

(defn next-page
  [catalog]
  (let [links (:links catalog [])]
    (when-let [next-link (first (filter #(= "next" (:rel %)) links))]
      (get-catalog (:href next-link)))))

(defn prev-page
  [catalog]
  (let [links (:links catalog [])]
    (when-let [prev-link (first (filter #(= "prev" (:rel %)) links))]
      (get-catalog (:href prev-link)))))

(comment
  ;; scroll through a providers holdings
  (let [catalog (atom (get-catalog "https://cmr.earthdata.nasa.gov/stac/GES_DISC"))]
    (while (not (nil? @catalog))
      #_(doseq [child (children @catalog)]
        (spit "ges_disc_stac.txt" (str (:href child) "\n") :append true))
      (swap! catalog next-page)))
  )
