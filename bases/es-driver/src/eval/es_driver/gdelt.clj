(ns eval.es-driver.gdelt
  (:require
   [clojure.string :as str]
   [eval.elastic.interface :as es]
   [eval.gdelt.interface.v2 :as gdeltv2])
  (:import
   [java.time Instant LocalDateTime ZoneOffset]
   [java.time.format DateTimeFormatter]))

(def gdelt-datetime-formatter (DateTimeFormatter/ofPattern "yyyyMMddHHmmss"))
(def gdelt-datetime-formatter-no-sec (DateTimeFormatter/ofPattern "yyyyMMddhh"))
(def gdelt-datetime-formatter-date-only (DateTimeFormatter/ofPattern "yyyyMMdd"))

(defn most-recent-quarter
  "Returns the most recent quarter hour. 00 15 30 45"
  [^LocalDateTime ldt]
  (* 15 (quot (.getMinute ldt) 15)))

(def gdelt-index-mapping {:mappings
                          {:properties
                           {:global-event-id {:type "integer"}
                            :dateadded {:type "date"
                                        :format "yyyyMMddHHmmss"}
                            :actor1-geo {:properties {:location {:type "geo_point"}}}
                            :actor2-geo {:properties {:location {:type "geo_point"}}}
                            :action-geo {:properties {:location {:type "geo_point"}}}}}})

(defn- dates-between
  "Returns an array of dates between the start and end in 15 minute intervals.
  If end is not specified, the most recent quarter hour will be used."
  [start & [end]]
  ;; TODO: validate input formats
  ;; TODO: validate end is not after (Instant/now)
  ;; TODO: validate start is not before start of GDELT dataset (2015 sometime)
  (let [end (or (and end (.parse gdelt-datetime-formatter end)) ;; TODO: check timezone malarky
                (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC))
        start-dt (LocalDateTime/parse start gdelt-datetime-formatter)]
    (loop [current start-dt
           datetimes []]
      (if (.isAfter current end)
        datetimes
        (recur (.plusMinutes current 15)
               (conj datetimes (.format gdelt-datetime-formatter current)))))))

(comment
  (dates-between "20200101000000")
  (dates-between "20200101000000" "20200201000000")
  )

(defn create-index
  [conn datetime & [index-mapping]]
  (let [prefix "gdelt-v2-events-"
        label (str prefix (.format gdelt-datetime-formatter-date-only datetime))
        index-mapping (or index-mapping gdelt-index-mapping)]
    (es/create-index conn label index-mapping)))

(defn harvest-since
  "Pulls gdelt values for the dates given and indexes them."
  [conn start-datetime]
  (let [dates (dates-between start-datetime)]
    (doseq [date dates]
      (let [label (str "gdelt-v2-events-" (str/join (take 8 date)))]
        (es/create-index conn label gdelt-index-mapping)
        (es/bulk-index conn
                       label
                       (gdeltv2/get-events date)
                       :global-event-id)))))

(defn harvest-latest
  "Pulls the latest gdeltv2 events manifest and indexes them in elasticsearch.
   The index is 'gdelt-v2-events-yyyyMMdd' with the corresponding date substituted."
  [conn]
  (let [events (gdeltv2/get-latest-events)
        now (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC)
        date (.format
              gdelt-datetime-formatter-no-sec
              now)
        minute (most-recent-quarter now)
        datetime (str date minute "00")
        index-label (str "gdelt-v2-events-" datetime)
        index gdelt-index-mapping]
    (es/create-index conn index-label index)
    (doseq [event events]
      ;; TODO convert to bulk-index
      (es/index-document conn index-label event (:global-event-id event)))))
