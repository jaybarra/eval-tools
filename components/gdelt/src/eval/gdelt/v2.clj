(ns eval.gdelt.v2
  (:require
   [clj-http.client :as client]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as spec]
   [clojure.string :as str]
   [eval.gdelt.util :as util]
   [taoensso.timbre :as log])
  (:import
   [java.util.zip ZipInputStream]
   [java.time LocalDateTime]
   [java.time.format DateTimeFormatter]))
  
(def ^:private datetime-fmt (DateTimeFormatter/ofPattern "yyyyMMddHHmmss"))
(def ^:private datetime-rx #"\d{10}(00|15|30|45)00")
(defn valid-datetime? [dt formatter]
  (try
    (if-let [_ (LocalDateTime/from (.parse formatter dt))]
      true
      false)
    (catch Exception e
      (log/error (format "Datetime [ %s ] is not valid" dt) (.getMessage e))
      false)))
(spec/def ::datetime (spec/and string?
                               #(valid-datetime? % datetime-fmt)
                               #(re-matches datetime-rx %)))

(defn- parse-manifest-line
  [line]
  (let [splits (str/split line #"\s+")
        url (nth splits 2)]
    {:id (re-find #"\d{14}" url) ; use the timestamp as the id
     :size (Integer/valueOf (first splits))
     :hash (second splits)
     :url url}))

(defn tsv->event
  "Parse a gdeltv2 TSV string into a map."
  [tsv]
  (let [fields (str/split tsv #"\t")
        actor1 {:code (nth fields 5)
                :name (nth fields 6)
                :country-code (nth fields 7)
                :known-group-code (nth fields 8)
                :ethnic-code (nth fields 9)
                :religion-codes (remove str/blank? [(nth fields 10) (nth fields 11)])
                :type-codes (remove str/blank? [(nth fields 12) (nth fields 13) (nth fields 14)])}
        actor1-loc (when-not (str/blank? (nth fields 40))
                     {:lat (edn/read-string (nth fields 40))
                      :lon (edn/read-string (nth fields 41))})
        actor1-geo {:type (nth fields 35)
                    :fullname (nth fields 36)
                    :country-code (nth fields 37)
                    :adm1-code (nth fields 38)
                    :adm2-code (nth fields 39)
                    :location actor1-loc
                    :feature-id (nth fields 42)}
        actor2 {:code (nth fields 15)
                :name (nth fields 16)
                :country-code (nth fields 17)
                :known-group-code (nth fields 18)
                :ethnic-code (nth fields 19)
                :religion-codes (remove str/blank? [(nth fields 20) (nth fields 21)])
                :type-codes (remove str/blank? [(nth fields 22) (nth fields 23) (nth fields 24)])}
        actor2-loc (when-not (str/blank? (nth fields 48))
                     {:lat (edn/read-string (nth fields 48))
                      :lon (edn/read-string (nth fields 49))})
        actor2-geo {:type (nth fields 43)
                    :fullname (nth fields 44)
                    :country-code (nth fields 45)
                    :adm1-code (nth fields 46)
                    :adm2-code (nth fields 47)
                    :location actor2-loc
                    :feature-id (nth fields 50)}
        action-loc (when-not (str/blank? (nth fields 56))
                     {:lat (edn/read-string (nth fields 56))
                      :lon (edn/read-string (nth fields 57))})
        action-geo {:type (nth fields 51)
                    :fullname (nth fields 52)
                    :country-code (nth fields 53)
                    :adm1-code (nth fields 54)
                    :adm2-code (nth fields 55)
                    :location action-loc
                    :feature-id (nth fields 58)}]
    {:global-event-id (nth fields 0)
     :day (nth fields 1)
     :month-year (nth fields 2)
     :year (nth fields 3)
     :fraction-date (nth fields 4)
     :actor1 actor1
     :actor2 actor2
     :is-root-event (nth fields 25)
     :event-code (nth fields 26)
     :event-base-code (nth fields 27)
     :event-root-code (nth fields 28)
     :quad-class (nth fields 29)
     :goldstein-scale (nth fields 30)
     :num-mentions (nth fields 31)
     :num-sources (nth fields 32)
     :num-articles (nth fields 33)
     :avg-tone (nth fields 34)
     :actor1-geo actor1-geo
     :actor2-geo actor2-geo
     :action-geo action-geo
     :dateadded (nth fields 59)
     :sourceurl (nth fields 60)}))

(defn tsv->events
  [lines]
  (map tsv->event lines))

(defn manifest->data
  [manifest]
  (-> manifest
      :url
      (client/get {:as :byte-array})
      :body
      io/input-stream
      ZipInputStream.
      util/gdelt-zip->tsv))

(defn get-events
  [datetime]
  (when-not (spec/valid? ::datetime datetime)
    (throw (ex-info "Invalid datetime" (spec/explain-data ::datetime datetime))))
  (log/debug "Getting GDelt V2 events for [" datetime "]")
  (-> (client/get (format "http://data.gdeltproject.org/gdeltv2/%s.export.CSV.zip"
                          datetime)
                  {:as :byte-array})
      :body
      io/input-stream
      ZipInputStream.
      util/gdelt-zip->tsv
      tsv->events))

(defn get-latest-events
  []
  (let [latest (slurp "http://data.gdeltproject.org/gdeltv2/lastupdate.txt")
        manifest (parse-manifest-line (first (str/split latest #"\n")))]
    (get-events (:id manifest))))
