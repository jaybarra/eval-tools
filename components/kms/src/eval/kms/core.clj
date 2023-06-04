(ns eval.kms.core
  (:require
   [camel-snake-kebab.core :as csk]
   [clj-http.client :as client]
   [clojure.data.csv :as csv]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.xml :as xml]
   [clojure.zip :as zip]
   [eval.utils.interface :as util]))

(def num-header-lines
  "Number of lines which contain header information in CSV files (not the actual keyword values)."
  2)

(def schemes
  #{:granuledataformat
    :idnnode
    :instruments
    :isotopiccategory
    :locations
    :mimetype
    :measurementname
    :platforms
    :projects
    :providers
    :rucontenttype
    :sciencekeywords
    :temporalresolutionrange})

(def keyword-scheme->field-names
  (-> "schemas/cmr-kms-hierarchy-12.edn"
      io/resource
      slurp
      edn/read-string))

(def keyword-scheme->expected-field-names
  "Maps each keyword scheme to the expected field names to be returned by KMS. We changed
  the names of some fields to provide a better name on our API."
  (merge keyword-scheme->field-names
         {:providers [:bucket-level-0 :bucket-level-1 :bucket-level-2 :bucket-level-3 :short-name
                      :long-name :data-center-url :uuid]
          :spatial-keywords [:location-category :location-type :location-subregion-1
                             :location-subregion-2 :location-subregion-3 :uuid]}))

(defn- validate-subfield-names
  "Validates that the provided subfield names match the expected subfield names for the given
  keyword scheme. Throws an exception if they do not match."
  [keyword-scheme subfield-names]
  (let [expected-subfield-names (keyword-scheme keyword-scheme->expected-field-names)]
    (when-not (= expected-subfield-names subfield-names)
      (throw (ex-info "Invalid subfield names"
                      {:actual subfield-names
                       :expected expected-subfield-names
                       :schema  keyword-scheme})))))

(def keyword-scheme->leaf-field-name
  "Maps each keyword scheme to the subfield which identifies the keyword as a leaf node."
  {:providers :short-name
   :platforms :short-name
   :instruments :short-name
   :projects :short-name
   :temporal-keywords :temporal-resolution-range
   :spatial-keywords :uuid
   :science-keywords :uuid
   :measurement-name :object
   :concepts :short-name
   :iso-topic-categories :uuid
   :related-urls :uuid
   :granule-data-format :uuid
   :mime-type :uuid})

(def keyword-scheme->required-field
  "Maps each keyword scheme to a field that must be present for a keyword to be valid."
  (merge keyword-scheme->leaf-field-name
         {:science-keywords :term
          :spatial-keywords :category
          :granule-data-format :uuid}))

(defn- parse-entries-from-csv
  "Parses the CSV returned by the GCMD KMS. It is expected that the CSV will be returned in a
  specific format with the first line providing metadata information, the second line providing
  a breakdown of the subfields for the keyword scheme, and from the third line on are the actual
  values.

  Returns a sequence of full hierarchy maps."
  [keyword-scheme csv-content]
  (let [all-lines (csv/read-csv csv-content)
        ;; Line 2 contains the subfield names
        kms-subfield-names (map csk/->kebab-case-keyword (second all-lines))
        _ (validate-subfield-names keyword-scheme kms-subfield-names)
        keyword-entries (->> all-lines
                             (drop num-header-lines)
                             (map #(zipmap (keyword-scheme keyword-scheme->field-names) %))
                             (map util/remove-blank-keys)
                             (filter (keyword-scheme->required-field keyword-scheme)))]
    keyword-entries))

(defn get-scheme
  [root scheme-type version]
  (when-not (schemes scheme-type)
    (throw (ex-info "Invalid scheme type" {:expected schemes
                                           :received scheme-type})))
  (when-not (string? version)
    (throw (ex-info "Invalid version" {:expected :string
                                       :received (type version)
                                       :version version})))
  (let [resp (client/get (format "%s%s"
                                 (if (str/ends-with? "/" root) root (str root "/"))
                                 (name scheme-type))
                         {:query-params (merge {:format "CSV"}
                                               (when version {:version version}))})]
    (when (= 200 (:status resp))
      (parse-entries-from-csv scheme-type (:body resp)))))

(def extract-version (comp first :content first))

(defn available-versions
  "Extracts the list of available version from a given endpoint."
  [url]
  (let [uri (str (if (str/ends-with? "/" url) url (str url "/"))
                 "concept_versions/version_type/past_published")
        root (-> uri
                 xml/parse
                 zip/xml-zip
                 zip/down)]
    (loop [node root
           versions []]
      (if node
        (recur (zip/right node)
               (conj versions (extract-version node)))
        versions))))

(comment
  (available-versions "https://gcmd.earthdata.nasa.gov/kms"))
