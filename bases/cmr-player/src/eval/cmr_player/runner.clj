(ns eval.cmr-player.runner
  (:require
   [clojure.string :as str]
   [eval.cmr.commands.ingest :as ingest]
   [eval.cmr.client :as client]))

(defn process-cmr-response
  [step resp]
  (let [{:keys [status headers body]} resp]
    (if (#{200 201} status)
      (println body)
      (printf "An problem occurred: %s\n" body))))

(defmulti run-step
  (fn [_client step]
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

(defmethod run-step :ingest
  [client step]
  (let [{{concept-type :concept-type
          concept-file :file
          provider :provider
          data-format :format
          native-id :native-id} :with} step
        command (ingest/create-concept
                 concept-type
                 provider
                 (slurp concept-file)
                 {:format data-format
                  :native-id native-id})]
    (process-cmr-response step (client/invoke client command))))

(defmethod run-step :delete
  [client step]
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

(defn play-script
  [client script]
  (doseq [step (:steps script)]
    (run-step client step)))
