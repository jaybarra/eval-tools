(ns build
  (:require
   [clojure.tools.build.api :as b]
   [org.corfield.build :as bb]))

(def lib 'eval/cmr-playback)
(def main 'eval.cmr-player.core)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean
  [opts]
  (-> opts
      (bb/clean)))

(defn uber
  [opts]
  (-> opts
      (assoc :lib lib
             :version version
             :uber-file uber-file
             :main main
             :transitive true)
      (bb/clean)
      (bb/uber)))
