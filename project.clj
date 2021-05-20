(defproject eval-tools "0.1.0-SNAPSHOT"
  :description "Evaluation and Testing Tools"
  :url "https://jaybarra.github.io/eval-tools"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[aero/aero "1.1.6"]
                 [clj-http/clj-http "3.12.1"]
                 [com.taoensso/carmine "3.1.0"]
                 [com.taoensso/timbre "5.1.2"]
                 [criterium/criterium "0.4.6"]
                 [crouton/crouton "0.1.2"]
                 [environ/environ "1.2.0"]
                 [integrant/integrant "0.8.0"]
                 [metosin/muuntaja "0.6.8"]
                 [metosin/reitit "0.5.13"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.3.618"]
                 [org.clojure/data.csv "1.0.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [ring/ring-core "1.9.3"]
                 [ring/ring-jetty-adapter "1.9.3"]]
  :plugins [[org.clojars.benfb/lein-gorilla "0.7.0"]]
  :profiles {:dev {:repl-options {:init (load-file "src/user.clj")
                                  :init-ns user}
                   :dependencies [[integrant/repl "0.3.2"]
                                  [org.clojure/test.check "1.1.0"]
                                  [ring/ring-devel "1.9.3"]
                                  [slamhound/slamhound "1.5.5"]]
                   :plugins [[jonase/eastwood "0.4.2"]
                             [lein-ancient/lein-ancient "0.6.15"]
                             [lein-cloverage/lein-cloverage "1.2.0"]
                             [lein-kibit/lein-kibit "0.1.8"]]}
             :kaocha {:dependencies [[lambdaisland/kaocha "1.0.829"]
                                     [lambdaisland/kaocha-cloverage "1.0.75"]
                                     [lambdaisland/kaocha-junit-xml "0.0.76"]]}}
  :repl-options {:init-ns eval.core}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
            "slamhound" ["run" "-m" "slam.hound"]})
