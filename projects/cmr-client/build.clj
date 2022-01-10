(ns build
  (:require 
   [clojure.tools.build.api :as b]
   [org.corfield.build :as bb]))

(def lib 'eval/cmr-client)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

#_(defn clean
  "Cleans the `target` directory."
  [_]
  (println "Cleaning target directory...")
  (b/delete {:path "target"}))

#_(defn uber
  "Builds an uberjar for the project."
  [_]
  (clean nil)
  (println "Copying source and resources...")
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (println "Compiling...")
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (println "Assembling uberjar...")
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'eval.cmr.cli.main})
  (println "Build complete"))
(defn uber [opts]
  (bb/uber {:class-dir class-dir
            :uber-file uber-file
            :basis basis
            :main 'eval.cmr.cli.main}))
