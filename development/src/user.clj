(ns user
  (:require
   [eval.hello.interface :as hello]))

(comment
  ;; Load the repl with the +default profile to get one version of hello
  ;; load the repl with the +hello profile to get a different

  (hello/greet "Bear"))
