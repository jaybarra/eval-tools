## API Guide

### eval.cmr.core

The central CMR interaction namespace.

```clojure
;; create a CMR enabled state object for the production environment
(def conn (cmr/cmr-conn :prod))

;; search prod for how many hits are available for a query
(cmr/cmr-hits conn {:provider "MY_PROVIDER"}) ;; => 12345
```

### eval.cmr.bulk.granule

Utilities specificailly for evaluating granule bulk update operations.

