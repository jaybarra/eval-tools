## CMR Evaluation Tool

A collection of utilities for evaluating the functionality of various components and aspects of NASA Common Metadata Repository.

### eval.cmr.core

The central CMR interaction namespace.

```clojure
;; create a CMR enabled state object for the production environment
(def state (cmr/cmr-state :prod))

;; search prod for how many hits are available for a query
(cmr/cmr-hits state {:provider "MY_PROVIDER}) ;; => 12345
```

### eval.cmr.bulk.granule

Utilities specificailly for evaluating granule bulk update operations.
