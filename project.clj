(defproject eval-tools "0.1.0-SNAPSHOT"
  :description "Evaluation and Testing Tools"
  :url "https://github.com/jaybarra/eval-tools"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clj-http "3.11.0"]
                 [com.taoensso/carmine "3.1.0"]
                 [com.taoensso/timbre "5.1.0"]
                 [crouton "0.1.2"]
                 [environ "1.2.0"]
                 [graphql-clj "0.2.9"]
                 [integrant "0.8.0"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.10"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]]
  :profiles {:dev {:repl-options {:init (load-file "dev/user.clj")
                                  :init-ns user}
                   :dependencies [[integrant/repl "0.3.2"]
                                  [org.clojure/test.check "1.1.0"]
                                  [ring/ring-devel "1.8.2"]
                                  [slamhound "1.5.5"]]
                   :plugins [[lein-ancient "0.6.15"]
                             [lein-cloverage "1.2.0"]
                             [jonase/eastwood "0.3.10"]
                             [lein-kibit "0.1.8"]]}
             :kaocha {:dependencies [[lambdaisland/kaocha "1.0.732"]
                                     [lambdaisland/kaocha-cloverage "1.0.75"]
                                     [lambdaisland/kaocha-junit-xml "0.0.76"]]}}
  :repl-options {:init-ns eval.core}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
            "slamhound" ["run" "-m" "slam.hound"]
            "test" "kaocha"})
