# CMR Evaluation Tool

A collection of utilities for evaluating the functionality of various components and aspects of NASA Common Metadata Repository.

## Usage

### configuration

```clojure
{:db/document-store {:type :crux}

 :app/cmr {:instances {:prod {:url "https://cmr.earthdata.nasa.gov"}
                       :uat {:url "https://cmr.uat.earthdata.nasa.gov"}
                       :sit {:url "https://cmr.sit.earthdata.nasa.gov"}
                       :local {:url "http://localhost"
                               :token "mock-echo-system-token"
                               :port-map {"access-control" 3011
                                          "search" 3003
                                          "ingest" 3002}}}
           :db #ig/ref :db/document-store}

 :handler/webapp {:welcome-message "Good luck!!"}

 :adapter/jetty {:handler #ig/ref :handler/webapp
                 :port 8880
                 :join? false}}
```

### eval.services.cmr

The difference between the `eval.cmr` and `eval.services.cmr` is the former is designed to handle commands sent to CMR only. Any logic or functionality beyond interaction with CMR belongs in a service. The services are higher level operations that can provide additional user features. 

### eval.cmr.core

The central CMR interaction namespace. This namespace provides low-level queries to CMR.

```clojure
;; create a CMR client for the production environment
(def client (cmr/create-client {:id :prod :url "https://cmr.earthdata.nasa.gov"}))

;; search CMR with a query for collection
(cmr/search client :collection {:provider "MY_PROVIDER"} {:format :umm-json})
```

## Documentation
* [API Docs](/api.md)

