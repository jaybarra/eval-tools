# CMR Evaluation Tool

A collection of utilities for evaluating the functionality of various components and aspects of NASA Common Metadata Repository.

## Usage

### configuration

```clojure
{;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 ;; Configuration
 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 :cmr {:prod {:url "https://cmr.earthdata.nasa.gov"}
       :uat {:url "https://cmr.uat.earthdata.nasa.gov"}
       :sit {:url "https://cmr.sit.earthdata.nasa.gov"}
       :local {:url "http://localhost"
               :token "mock-echo-system-token"
               :port-map {"access-control" 3011
                          "search" 3003
                          "ingest" 3002}}}

 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 ;; System Configs
 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 :db/event-store {}

 :app/core {:db #ig/ref :db/event-store}
 :app/cmr {:db #ig/ref :db/event-store}

 :handler/webapp {:welcome-message "Good luck!!"
                  :db #ig/ref :db/event-store}

 :adapter/jetty {:handler #ig/ref :handler/webapp
                 :port 8880
                 :join? false}}
```

### eval.cmr.core

The central CMR interaction namespace.

```clojure
;; create a CMR client for the production environment
(def client (cmr/create-client :prod))

;; search CMR with a query for collection
(cmr/search client :collection {:provider "MY_PROVIDER"} {:format :umm-json})
```

### eval.cmr.bulk.granule

Utilities specificailly for evaluating granule bulk update operations.
