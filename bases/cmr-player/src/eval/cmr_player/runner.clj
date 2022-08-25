(ns eval.cmr-player.runner
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [eval.cmr.interface.client :as client]
   [eval.cmr.interface.ingest :as ingest]
   [eval.cmr.interface.search :as search]
   [eval.elastic.interface :as es]
   [fipp.edn :refer [pprint] :rename {pprint fipp}]))

;; All derived methods should return the STATE
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
        (assoc state :search-after (get-in headers ["CMR-Search-After"])))
      (do
        (printf "Search failed%n")
        (fipp headers)
        (fipp body)
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

(defn- play-step
  "Executes a script step."
  [state step]
  (loop [state state
         iterations (get step :repeat 1)]
    (when (true? (:progress step))
      ;; TODO this is weak to division by zero
      ;; TODO this may not yield 80 characters all the time
      (let [pct-left (int (+ 0.5 (* 78 (/ iterations (get step :repeat 1)))))
            pct-done (int (+ 0.5 (* 78 (/ (- (get step :repeat 1) iterations) (get step :repeat 1)))))]
        ;; TODO does the carriage return always work?
        (printf "\r[%s%s]"
                (apply str (repeatedly pct-done (constantly "#")))
                (apply str (repeatedly pct-left (constantly "-"))))
        (flush)))
    (if-not (pos? iterations)
      (do
        (when (true? (:progress step)) (printf "%n"))
        state)
      (recur (execute-step state step)
             (dec iterations)))))

(defn play-script
  "Execute actions in a script sequentally."
  [state script]
  (loop [steps (:steps script)
         state (assoc state :step# 1)]
    (if-let [step (first steps)]
      (do
        (printf "Step [%3d] - %s%n" (:step# state) (:action step))
        (flush)
        (recur (rest steps)
               (-> state
                   (play-step step)
                   (update :step# inc))))
      (dissoc state :step#))))
