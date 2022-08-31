(ns eval.cmr-player.runner
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [eval.cmr.interface.client :as client]
   [eval.cmr.interface.ingest :as ingest]
   [eval.cmr.interface.search :as search]
   [eval.elastic.interface :as es]
   [fipp.edn :refer [pprint] :rename {pprint fipp}])
  (:import
   java.util.concurrent.Executors))

(defmulti execute-step
  (fn [_state step]
    (:action step)))

(defmethod execute-step :default
  [state step]
  (log/error  "No handler for action" step)
  state)

(defn unit->time
  [unit]
  (case (str/lower-case (name unit))
    "s" 1000
    "ms" 1
    "m" 60000))

(defmethod execute-step :wait
  [state step]
  (let [{{duration :duration
          msg :message} :with} step
        [time units] (drop 1 (re-matches #"(\d+)(\w+)" duration))]
    (if msg
      (do
        (println msg)
        (log/info msg))
      (log/info (format "Pausing for %s%s%n" time units)))
    (Thread/sleep (* (Integer/parseInt time 10) (unit->time units)))
    state))

(defmethod execute-step :say
  [state step]
  (let [{{msg :message} :with} step]
    (when msg
      (println msg)
      (log/info msg))
    state))

(defmethod execute-step :cmr/search-shapefile
  [{client :client
    search-after :search-after
    :as state} step]
  (let [{{concept-type :concept-type
          shapefile :shapefile
          mime-type :mime-type} :with
         silent? :silent} step
        command (search/search-after-post
                 concept-type
                 [{:name "shapefile"
                   :content (clojure.java.io/file shapefile)
                   :mime-type mime-type}]
                 search-after
                 nil)
        {:keys [body status headers]} (client/invoke client command)]
    (if (= 200 status)
      (do
        (when-not silent? (fipp body))
        (log/info (format "Shapefile search returned with status [%d]" status) headers body)
        (assoc state :search-after (get-in headers ["CMR-Search-After"])))
      (do
        (log/info (format "Shapefile search failed with status [%d]" status) headers body)
        (printf "Search failed%n")
        (when headers (fipp headers))
        (when body (fipp body))
        state))))

(defmethod execute-step :cmr/ingest
  [{client :client
    root :script-relative-root
    :as state} step]
  (let [{{concept-type :concept-type
          concept-file :file
          provider-id :provider
          data-format :format
          native-id :native-id} :with} step
        command (ingest/create-concept
                 concept-type
                 provider-id
                 (slurp (str root java.io.File/separator concept-file))
                 {:format data-format
                  :native-id native-id})
        {:keys [body status headers]} (client/invoke client command)]
    (if (#{200 201} status)
      (do
        (printf "Successfully ingested [%10s][%s][%s] as [%s]%n"
                (name concept-type) provider-id native-id
                (second (re-find #"<concept-id>(.*)</concept-id>" body)))
        (log/info (format "Successfully ingested [%s][%s][%s]"
                          (name concept-type)
                          provider-id
                          native-id)
                  body))
      (log/error (format "A problem occurred ingesting [%s][%s][%s]"
                         (name concept-type)
                         provider-id
                         native-id)
                 body))
    state))

(defmethod execute-step :cmr/delete
  [{client :client :as state} step]
  (let [{{concept-type :concept-type
          provider-id :provider
          native-id :native-id} :with} step
        command (ingest/delete-concept
                 concept-type
                 provider-id
                 native-id)
        {:keys [body header status] :as resp} (client/invoke client command)]
    (if (#{200 201} status)
      (log/info (format "Successfully deleted [%s][%s][%s]"
                        (name concept-type)
                        provider-id
                        native-id)
                body)
      (log/error (format "A problem occurred deleting [%s][%s][%s]"
                         (name concept-type)
                         provider-id
                         native-id)
                 body))
    state))

(defmethod execute-step :es/delete-by-query
  [state {{host :host index :index query :query} :with}]
  (printf "Ran direct deletion%n")
  (es/delete-by-query {:url host} index query)
  state)

(defn- print-progress
  [current total & opts]
  (let [width (get opts :width 80)
        ;; TODO limit message length
        message (get opts :message "")
        pct-left (int (+ 0.5 (* width (/ current total))))
        pct-done (int (+ 0.5 (* (- width (count message)) (/ (- total current) total))))]
    (printf "\r[%s%s%s]"
            message
            (apply str (repeatedly pct-done (constantly "#")))
            (apply str (repeatedly pct-left (constantly "-"))))
    (flush)))

(defn- duration->ms
  "Convert shorthand duration notation to milliseconds.
  Supported units: sec,min,hour"
  [duration]
  (if (seq duration)
    (let [quantity (Integer/parseInt (or (re-find #"\d+" duration) "0"))
          unit (str/lower-case (or (re-find #"[a-zA-Z]+" duration) "s"))]
      (case unit
        "s" (* quantity 1000)
        "sec" (* quantity 1000)
        "second" (* quantity 1000)
        "seconds" (* quantity 1000)
        "m" (* quantity 60000)
        "min" (* quantity 60000)
        "minute" (* quantity 60000)
        "minutes" (* quantity 60000)
        "h" (* quantity 360000)
        "hour" (* quantity 360000)
        "hours" (* quantity 360000)
        ;; default
        0))
    0))

(defn- play-step-runner
  "Executes action of a STEP."
  [state step]
  (loop [state state
         iterations (get step :repeat 1)]
    (log/info (format "Playing step [%d/%d]"
                      (- (get step :repeat 1) iterations)
                      (get step :repeat 1))
              step)
    (when (and (pos? (get step :repeat 1))
               (true? (:progress step)))
      (print-progress iterations (get step :repeat 1)))
    (if-not (pos? iterations)
      state
      (recur (execute-step state step)
             (dec iterations)))))

(defn- play-step
  "Executes a script step."
  [state step]
  (let [start (get state :step-start (System/currentTimeMillis))
        duration (duration->ms (get step :loop))
        _ (assoc state :step-start start)
        end-state (play-step-runner state step)]
    (when (<= (System/currentTimeMillis) (+ start duration)) (play-step end-state step))
    (when (true? (:progress step)) (printf "%n"))
    (dissoc end-state :step-start)))

(defn- play-step-wrapper
  [state step]
  ;; TODO validate concurrency is a positive int
  (let [n#threads (get step :concurrency 1)
        pool (Executors/newFixedThreadPool n#threads)
        tasks (map (fn[t]
                     (fn []
                       (log/debug (format "Starting step thread [%s][%s]" t (:action step)) )
                       (play-step state (if (zero? t)
                                          step
                                          ;; only print to stdout on the 0th thread, others will go to logs
                                          (assoc step :silent true)))))
                   (range n#threads))]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
    (.shutdown pool)
    ;; THIS IS NOT THE CORRECT STATE
    ;; TODO figure out how to merge state updated by concurrent threads, or if that's even necessary
    state))

(defn play-script
  "Execute actions in a script sequentally."
  [state script]
  (loop [steps (:steps script)
         state (assoc state :step# 1)]
    (if-let [step (first steps)]
      (do
        (printf "Step [%d/%d] - %s%s%n"
                (:step# state)
                (count (:steps script))
                (:action step)
                ;; TODO format step meta more generically
                (str (if-let [loop (:loop step)] (format " Looping [%s]" loop) "")
                     (if-let [threads (:concurrency step)] (format " Concurrent [%s]" threads) "")))
        (flush)
        (log/info "Playing step" step)
        (recur (rest steps)
               (-> state
                   (play-step-wrapper step)
                   (update :step# inc))))
      (dissoc state :step#))))
