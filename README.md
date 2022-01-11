# Eval Tools

<p align="center">
  <a href="https://github.com/jaybarra/eval-tools/actions/workflows/test.yml">
    <img alt="Test Status"
         src="https://github.com/jaybarra/eval-tools/workflows/Run%20Unit%20Tests/badge.svg"/>
  </a>

  <a href="https://github.com/jaybarra/eval-tools/actions/workflows/lint.yml">
    <img alt="Linter Report"
         src="https://github.com/jaybarra/eval-tools/workflows/Lint%20Code%20Base/badge.svg"/>
  </a>
</p>

A Clojure suite designed to assist with evaluating and testing external components.

## Approach

This tool is designed to interact with external APIs and tools, with simplified commands and clients to invoke those commands. The approach to the commands is to be low-level and involve only basic query and response communication with no higher logic applied. All commands should be stateless.

```clojure
(require '[eval.cmr.interface.client :as cmr])
(require '[eval.cmr.interface.search :refer [search]])
(let [client (cmr/create-client {:url "http://cmr-instance"})
      command (search :collection 
                      {:provider "PROVIDER_ID"}
                      {:format :umm-json}))]
  (cmr/decode-cmr-response-body (cmr/invoke client command)))
```
## Developent

Poly Info

```bash
clojure -M:poly info
```
