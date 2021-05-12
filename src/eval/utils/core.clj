(ns eval.utils.core
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::positive-num (comp not neg?))

(defn spec-problems
  [spec value]
  (:clojure.spec.alpha/problems (s/explain-data spec value)))

(defn spec-validate
  "Check spec by value.
  Returns nil on pass, otherwise throws an exception"
  [spec value]
  (when-let [problem (->> (spec-problems spec value)
                          first
                          :via)]
    (throw (ex-info "Validation failed" {:validation problem}))))

(defn within?
  "Returns true if two numbers are nearly equal within a given delta.
  Should only be used for 6 significant digits or fewer.
  Eg (within? 0.1 2.4 2.41) => true"
  [delta a b]
  (spec-validate ::positive-num delta)
  (<= (- (double (Math/abs (- a b))) 1e-7)
      (double delta)))

(defmacro defn-timed
  "Creates a function that logs how long it took to execute the body. It
  supports multiarity functions but only times how long the last listed arity
  version takes. This means it should be used with multiarity functions where
  it calls itself with the extra arguments."
  [fn-name & fn-tail]
  (let [fn-name-str (name fn-name)
        ns-str (str *ns*)
        ;; Extract the doc string from the function if present
        [doc-string fn-tail] (if (string? (first fn-tail))
                               [(first fn-tail) (next fn-tail)]
                               [nil fn-tail])
        ;; Wrap single arity functions in a list
        fn-tail (if (vector? (first fn-tail))
                  (list fn-tail)
                  fn-tail)
        ;; extract other arities defined in the function which will not be
        ;; timed.
        other-arities (drop-last fn-tail)
        ;; extract the last arity definitions bindings and body
        [timed-arity-bindings & timed-arity-body] (last fn-tail)]
    `(defn ~fn-name
       ~@(when doc-string [doc-string])
       ~@other-arities
       (~timed-arity-bindings
        (let [start# (System/currentTimeMillis)]
          (try
            ~@timed-arity-body
            (finally
              (let [elapsed# (- (System/currentTimeMillis) start#)]
                (when (true? (get (system/config) :benchmarking?))
                  (log/info
                   (format
                    "Timed function [%s/%s] took [%d] ms."
                    ~ns-str ~fn-name-str elapsed#)))))))))))
