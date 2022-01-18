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

(def gdelt-v2-earliest-entry (LocalDateTime/parse
                              "20150218230000"
                              gdelt-datetime-formatter))

(defn- most-recent-quarter
  "Returns a LocalDateTime with the most recent quarter hour. 00 15 30 45"
  [^LocalDateTime ldt]
  (let [quarter (* 15 (quot (.getMinute ldt) 15))]
    (-> ldt
        (.withMinute quarter)
        (.withSecond 0)
        (.withNano 0))))

(def gdelt-index-mapping {:mappings
                          {:properties
                           {:global-event-id {:type "long"}
                            :day {:type "date"
                                  :format "yyyyMMdd"}
                            :month-year {:type "date"
                                         :format "yyyyMM"}
                            :year {:type "date"
                                   :format "yyyy"}
                            :quad-class {:type "integer"}
                            :goldstein-scale {:type "float"}
                            :num-mentions {:type "integer"}
                            :num-sources {:type "integer"}
                            :num-articles {:type "integer"}
                            :avg-tone {:type "float"}
                            :dateadded {:type "date"
                                        :format "yyyyMMddHHmmss"}
                            :actor1-geo {:properties {:type {:type "integer"}
                                                      :location {:type "geo_point"}}}
                            :actor2-geo {:properties {:type {:type "integer"}
                                                      :location {:type "geo_point"}}}
                            :action-geo {:properties {:type {:type "integer"}
                                                      :location {:type "geo_point"}}}}}})

(defn- dates-between
  "Returns an array of dates between the start and end in 15 minute intervals.
  If end is not specified, the most recent quarter hour will be used.
  The datetimes are formatted in yyyyMMddHHmmss to correspond with GDelt URL entries"
  [start & [end]]
  ;; TODO: validate input formats
  (let [start-dt (if (string? start)
                   (LocalDateTime/parse start gdelt-datetime-formatter)
                   start)
        end-dt (or (and end 
                        (if (string? end)
                          (.parse gdelt-datetime-formatter end)
                          end))
                (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC))]
    (when (.isAfter start-dt end-dt)
      (throw (ex-info "Invalid date"
                      {:message "Start cannot be after the end"
                       :start start-dt
                       :end end-dt})))
    (when (.isBefore start-dt gdelt-v2-earliest-entry)
      (throw (ex-info "Invalid date" {:message "Start cannot be before earliest GDelt entry"
                                      :start start
                                      :earliest gdelt-v2-earliest-entry})))
    (loop [current start-dt
           datetimes []]
      (if (.isAfter current end-dt)
        datetimes
        (recur (.plusMinutes current 15)
               (conj datetimes (.format gdelt-datetime-formatter current)))))))

(comment
  (dates-between "20220101000000")
  (dates-between (most-recent-quarter (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC)))
  )

(defn create-index
  [conn datetime & [index-mapping]]
  (let [prefix "gdelt-v2-events-"
        label (str prefix (.format gdelt-datetime-formatter-date-only datetime))
        index-mapping (or index-mapping gdelt-index-mapping)]
    (es/create-index conn label index-mapping)))

(defn harvest-between
  "Pulls gdelt values for the dates given and indexes them."
  [conn start-dt end-dt]
  (let [start-dt (if (string? start-dt)
                   (LocalDateTime/parse start-dt gdelt-datetime-formatter)
                   start-dt)
        start-dt (most-recent-quarter start-dt)
        end-dt (most-recent-quarter end-dt)
        dates (dates-between start-dt end-dt)]
    (doseq [date dates]
      (let [label (str "gdelt-v2-events-" (str/join (take 8 date)))]
        (es/create-index conn label gdelt-index-mapping)
        (es/bulk-index conn
                       label
                       (gdeltv2/get-events date)
                       {:id-field :global-event-id})))))

(defn harvest-since
  "Pulls GDelt V2 events and indexes them."
  [conn start-dt]
  (harvest-between
   conn
   start-dt
   (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC)))

(defn harvest-latest
  "Pulls the latest gdeltv2 events manifest and indexes them in elasticsearch.
   The index is 'gdelt-v2-events-yyyyMMdd' with the corresponding date substituted."
  [conn]
  (let [events (gdeltv2/get-latest-events)
        now (LocalDateTime/ofInstant (Instant/now) ZoneOffset/UTC)
        date (.format
              gdelt-datetime-formatter-no-sec
              now)
        minute (.getMinute (most-recent-quarter now))
        datetime (str date minute "00")
        index-label (str "gdelt-v2-events-" datetime)
        index gdelt-index-mapping]
    (es/create-index conn index-label index)
    (doseq [event events]
      ;; TODO convert to bulk-index
      (es/index-document conn index-label event (:global-event-id event)))))
