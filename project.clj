(defproject eval-tools "0.1.0-SNAPSHOT"
  :description "Evaluation and Testing Tools"
  :url "https://github.com/jaybarra/eval-tools"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[cheshire "5.10.0"]
                 [clj-http "3.10.1"]
                 [crouton "0.1.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.2.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]

                 ;; purposely out of date
                 [com.fasterxml.jackson.core/jackson-databind "2.6.4"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [slamhound "1.5.5"]]
                   :plugins [[lein-cloverage "1.2.0"]
                             [jonase/eastwood "0.3.10"]
                             [lein-kibit "0.1.8"]]}
             :kaocha {:dependencies [[lambdaisland/kaocha "1.0.700"]
                                     [lambdaisland/kaocha-cloverage "1.0.63"]
                                     [lambdaisland/kaocha-junit-xml "0.0.76"]]}}
  :repl-options {:init-ns eval.core}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
            "slamhound" ["run" "-m" "slam.hound"]
            "test" "kaocha"})
