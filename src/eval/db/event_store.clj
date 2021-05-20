(ns eval.db.event-store)

(defprotocol EventStore
  "A protocol for implementing an event stream for event-sourcing."
  (retrieve-event-stream
    [this aggregate-id]
    "Retrieve an event stream for an aggregate")

  (append-events! [this aggregate-id prior-stream events]
    "Append new events to a stream and return the stream.
    when events are appended the version increments."))

(defrecord EventStream [version transactions])

(defrecord LocalStore [data-atom]

  EventStore

  (retrieve-event-stream [this aggregate-id]
    (get-in @data-atom [aggregate-id] (->EventStream 0 [])))

  (append-events! [this aggregate-id prior-stream events]
    (let [next-stream (->EventStream
                       (inc (:version prior-stream))
                       (conj (:transactions prior-stream) events))]
      (swap! (:data-atom this) assoc aggregate-id next-stream)
      next-stream)))
