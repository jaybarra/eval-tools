(ns build
  "CMR Client
  
  clojure -T:build ci"
  (:require 
   [clojure.tools.build.api :as b]
   [org.corfield.build :as bb]))

(def lib 'eval/cmr-client)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn ci "Run the CI pipeline of tests (and build the Uber JAR)." [opts]
  (let [opts (or opts {})]
    (-> opts
        (assoc :lib lib 
               :version version 
               :uber-file uber-file
               :main 'eval.cmr.cli.main)
        (bb/run-tests)
        (bb/clean)
        (bb/uber))))
