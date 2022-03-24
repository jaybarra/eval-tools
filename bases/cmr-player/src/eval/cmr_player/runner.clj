(ns eval.cmr-player.runner
  (:require
   [clojure.string :as str]
   [eval.cmr.interface.ingest :as ingest]
   [eval.cmr.interface.client :as client]
   [eval.elastic.interface :as es]))

(defn process-cmr-response
  [step resp]
  (let [{:keys [status headers body]} resp]
    (if (#{200 201} status)
      (println body)
      (printf "An problem occurred: %s\n" body))))

(defmulti run-step
  (fn [_state step]
    (:action step)))

(defmethod run-step :default
  [_ step]
  (printf "No handler for [%s] action%n" (:action step))
  (clojure.pprint/pprint step))

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
      (println msg)
      (printf "Pausing for %s%s%n" time units))
    (Thread/sleep (* (Integer/parseInt time 10) (unit->time units)))))

(defmethod run-step :say
  [_ step]
  (let [{{msg :message} :with} step]
    (when msg
      (println msg))))

(defmethod run-step :cmr/ingest
  [{client :client
    root :script-relative-root} step]
  (let [{{concept-type :concept-type
          concept-file :file
          provider :provider
          data-format :format
          native-id :native-id} :with} step
        command (ingest/create-concept
                 concept-type
                 provider
                 (slurp (str root java.io.File/separator concept-file))
                 {:format data-format
                  :native-id native-id})]
    (process-cmr-response step (client/invoke client command))))

(defmethod run-step :cmr/delete
  [{client :client} step]
  (let [{{concept-type :concept-type
          concept-file :file
          provider :provider
          data-format :format
          native-id :native-id} :with} step
        command (ingest/delete-concept
                 concept-type
                 provider
                 native-id)]
    (process-cmr-response step (client/invoke client command))))

(defmethod run-step :es/delete-by-query
  [_ {{host :host index :index query :query} :with}]
  (es/delete-by-query {:url host} index query))

(defn play-script
  [state script]
  (doseq [step (:steps script)]
    (run-step state step)))
