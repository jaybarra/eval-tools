# API Guide

## eval.cmr.core

The central CMR interaction namespace.

## Examples

### Get a list of ACLs
```clojure
(go)                                            ;; start the service and load in the configuration
(require '[eval.cmr.core :as cmr])              ;; required for sending commands to CMR
(require '[eval.cmr.acls :as acls])             ;; required for generating ACL commands
(require '[eval.services.cmr.core :as cmr-svc]) ;; used for extracting clients instead of making it manually

(let [client (cmr-svc/context->client (context) :local)
      response (cmr/invoke client (acls/get-acls))
      acls (-> response
               cmr/decode-cmr-response-body
               :items)]
  (-> response
      cmr/decode-cmr-response-body
      :items))
```

### Get a list of tags
```clojure
(->> (search (context) :prod :tag {:page_size 100})
     :items
     (map :tag_key))
```
