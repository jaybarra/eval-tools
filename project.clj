(defproject maths "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[com.taoensso/timbre "4.10.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [org.clojure/test.check "0.9.0"]
                                  [slamhound "1.5.5"]]
                   :plugins [[jonase/eastwood "0.3.10"]
                             [lein-kibit "0.1.8"]]}
             :kaocha {:dependencies [[lambdaisland/kaocha "1.0.663"]]}}
  :repl-options {:init-ns maths.core}
  :aliases {"slamhound" ["run" "-m" "slam.hound"]
            "kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]})
