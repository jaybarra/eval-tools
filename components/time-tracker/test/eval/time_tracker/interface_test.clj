(ns eval.time-tracker.interface-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [eval.time-tracker.interface :as time-tracker]))

(deftest start--given-when-then
  (testing "Given an empty log and an event"
    (let [event "painting"]
      (testing "when I start tracking the event"
        (let [new-log (time-tracker/start nil "painting")]
          (is (= 1 (count new-log))
              "Then there is a log with a single event returned")
          
          (is (= {:event event
                  :type :start}
                 (select-keys (last new-log) [:event :type]))
              "Then then a :start event is added to the log")
          
          (is (contains? (last new-log) :timestamp)
              "Then the log contains a timestamp of when the event began"))))))

(deftest stop--empty-log--log-with-stop-event
  (let [log (time-tracker/stop nil "cleaning")]
    (is (= 1 (count log)))
    (is (= {:event "cleaning"
            :type :stop}
           (select-keys (last log) [:event :type])))
    (is (contains? (last log) :timestamp))))
