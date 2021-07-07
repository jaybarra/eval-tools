# CMR Evaluation Tool

A collection of utilities for evaluating the functionality of various components and aspects of NASA Common Metadata Repository.

## Usage

### Configuration

__Example config.edn__
```clojure
{:db/document-store {:type :crux}

 :app/cmr {:instances {:prod {:url "https://cmr.earthdata.nasa.gov"
                       :echo-token #env "CMR_ECHO_TOKEN_PROD"}

                       :uat {:url "https://cmr.uat.earthdata.nasa.gov"
                       :echo-token "fixed-token-value"}

                       :local {:url "http://localhost"
                               :endpoints {:search "http://localhost:3003"
                                           :access-control "http://localhost:3011"}}
           :db #ig/ref :db/document-store}

 :handler/webapp {:welcome-message "Good luck!!"}

 :adapter/jetty {:handler #ig/ref :handler/webapp
                 :port 8880
                 :join? false}}
```

#### CMR Configuration
A CMR config map consists of a map of maps.

Eval Tools allows for multiple CMR clients to be configured simulaneously. Each CMR connection should be placed into the `:instances` map of `:app/cmr`.
The identifier should be a keyword. A corresponding configuration map should be the value.

The configuration map must include the `:url`. `:echo-token` is optional and may be an enviroment value or a static string. If no value is set all calls to CMR will be anonymous and the `Echo-Token` header will not be set unless supplied to the client manually in the request.

And may optionally include a `:endpoints` map to override the `:url` specified earlier. This endpoints map is useful when CMR is deployed locally in development mode or not hosted behind a reverse proxy or load-balancer. The endpoints should be a map consisting of the appropriate service name followed by the url of the service. See the above configuration for example

#### Aero config tags
To set properties from the environment, use the `#env` keyword followed by the environment variable.

#### Supported Document Stores

| Key     | Description                                               | Options                                                                     |
|:--------|:----------------------------------------------------------|:----------------------------------------------------------------------------|
| `:crux` | Bi-temporal graph document store<br> Single node instance | `:log-dir` - optional<br> `:doc-dir` - optional<br> `:index-dir` - optional |
| `:noop` | Prints to console                                         |                                                                             |

### eval.services.cmr
The difference between the `eval.cmr` and `eval.services.cmr` is the former is designed to handle commands sent to CMR only. Any logic or functionality beyond interaction with CMR belongs in a service. The services are higher level operations that can provide additional user features. 

### eval.cmr.core
The central CMR interaction namespace. This namespace provides low-level queries to CMR.

```clojure
;; create a CMR client for the production environment
(def client (cmr/create-client {:id :prod :url "https://cmr.earthdata.nasa.gov"}))

;; search CMR with a query for collection
(cmr/invoke client :collection {:provider "MY_PROVIDER"} {:format :umm-json})
```

## Documentation
* [API Docs](/api.md)

