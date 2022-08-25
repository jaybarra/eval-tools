(ns eval.cmr-player.runner
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [eval.cmr.interface.ingest :as ingest]
   [eval.cmr.interface.client :as client]
   [eval.elastic.interface :as es]))

(defmulti run-step
  (fn [_state step]
    (:action step)))

(defmethod run-step :default
  [_ step]
  (log/error  "No handler for action" step))

(defn unit->time
  [unit]
  (case (str/lower-case (name unit))
    "s" 1000
    "ms" 1
    "m" 60000))

(defmethod run-step :wait
  [_ step]
  (let [{{duration :duration
          msg :message} :with} step
        [time units] (drop 1 (re-matches #"(\d+)(\w+)" duration))]
    (if msg
      (do
        (println msg)
        (log/info msg))
      (log/info (format "Pausing for %s%s%n" time units)))
    (Thread/sleep (* (Integer/parseInt time 10) (unit->time units)))))

(defmethod run-step :say
  [_ step]
  (let [{{msg :message} :with} step]
    (when msg
      (println msg)
      (log/info msg))))

(defmethod run-step :cmr/ingest
  [{client :client
    root :script-relative-root} step]
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
        {:keys [body status headers]}(client/invoke client command)]
    (if (#{200 201} status)
      (do
        (printf "Successfully ingested [%10s][%s][%s] as [%s]%n"
                (name concept-type)
                provider-id
                native-id
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
                 body))))

(defmethod run-step :cmr/delete
  [{client :client} step]
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
                 body))))

(defmethod run-step :es/delete-by-query
  [_ {{host :host index :index query :query} :with}]
  (printf "Ran direct deletion%n")
  (es/delete-by-query {:url host} index query))

(defn play-script
  [state script]
  (doseq [step (:steps script)]
    (run-step state step))
  (printf "Script complete%n"))
