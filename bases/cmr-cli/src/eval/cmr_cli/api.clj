(ns eval.cmr-cli.api
  (:require
   [clojure.string :as str]
   [eval.cmr.interface :as cmr]
   [eval.cmr.interface.search :as search]))

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
      "search" (do (println args)
                 [client (search/search args)])

      ;; default
      (println "unrecognized command [" command "]"))))

(defn search
  "Given a command map arguments and invoke the command"
  [args]
  (let [[client command] (command-handler nil args)]
    (cmr/invoke client command)))
