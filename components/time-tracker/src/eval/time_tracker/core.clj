(ns eval.time-tracker.core
  (:import
   [java.time LocalDateTime]))

(defn start
  [log event]
  (let [log (or log [])]
    (conj log {:event event
               :type :start
               :timestamp (.toString (LocalDateTime/now))})))

(defn stop
  [log event]
  (let [log (or log [])]
    (conj log {:event event
               :type :stop
               :timestamp (.toString (LocalDateTime/now))})))

(defn intervals
  [timestamps]

  (map #(LocalDateTime/parse %) timestamps)
  )

(comment
  (intervals ["23-May-2023 14:47:47"
              "23-May-2023 15:13:40"])

  )
