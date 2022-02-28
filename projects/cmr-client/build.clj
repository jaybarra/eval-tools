(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'eval/cmr-cli)
(def main 'eval.cmr-cli.core)
(def version (format "1.0.%s" (b/git-count-revs nil)))

(defn clean
  [opts]
  (-> opts
      bb/clean))

(defn uber
  [opts]
  (-> opts
      clean
      (assoc :lib lib :main main :version version :transitive true)
      bb/uber))
