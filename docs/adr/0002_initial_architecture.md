# 0002 Initial Architechture

## Status

__in progress__

## Context

Usaed deps.edn over leiningen

Eval Tools exists in three parts
- CMR commmands namespace
  - clojure
  - current integrated with the rest of the server code
  - pure enough that it can be pulled into separate jar
- Eval Tools server (clojure)
  - jetty as server
  - integrant as compnentizer
  - reitit as router
- Eval Tools webapp
  - clojurescript
  - shadowcljs
  - reagent/react
  
## Decisions

`deps.edn` was used to get familiarity and not rely on plugins

Shadow-cljs was chosen over fighweel since it had clearer documentation for use with `deps.edn`

## Consequences
  
If clojurescript is compiled and hosted via server it could run as a monolith.
