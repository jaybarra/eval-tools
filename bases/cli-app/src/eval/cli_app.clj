(ns eval.cli-app
  (:gen-class)
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.string :as str]
   [eval.services.cmr.search :as search]))

(spec/def ::arg-list (spec/cat :command string?
                               :cmr keyword?
                               :query-params (spec/? map?)
                               :options (spec/? map?)))

(defn command-handler
  [context args]
  (println args)
  (let [args (str/split args #" ")
        command (first args)
        client-id (keyword (second args))
        client (get-in context [:instances client-id])
        args (drop 2 args)]
    (when-not client
     {::fault (ex-info "Invalid command"
                       {:error "No such client [" client-id "]"
                        :context context})})
    (case command
      "search" (search/search client (keyword (first args)) {} {:format :umm-json})
      (do
        (println "unrecognized command [" command "]")
        {:exit true}))))

(defn create-app
  [cfg]
  (loop []
    (flush)
    (print "Enter: ")
    (let [in (str/trim (read-line))]
      (case in
        "quit" :quit
        "help" (do (println "help coming soon, \"quit\" to quit")
                   (recur))
        (do
          (command-handler cfg in)
          (recur))))))
