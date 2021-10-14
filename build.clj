(ns build
  (:require
   [clojure.tools.build.api :as b]))

(def lib 'eval/cmr)

(def version (format "0.0.%s" (b/git-count-revs nil)))
(def class-dir "modules/cmr/target/classes")
(def basis (b/create-basis {:project "modules/cmr/deps.edn"}))
(def jar-file (format "modules/cmr/target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "modules/cmr/target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))
