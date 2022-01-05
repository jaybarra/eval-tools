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

A Clojure library designed to assist with evalutating and testing REST
APIs and tools.

## Approach

This tool is designed to interact with external APIs, with simplified commands and clients to invoke those commands. The approach to the commands is to be low-level and involve only basic query and response communication with no higher logic applied. All commands should be stateless.

```clojure
(require '[eval.cmr.client :as cmr])
(require '[eval.cmr.commands.search :refer [search]])
(let [client (cmr/create-client {:url "http://cmr-instance"})
      command (search :collection 
                      {:provider "PROVIDER_ID"}
                      {:format :umm-json}))]
  (cmr/decode-cmr-response-body (cmr/invoke client command)))
```

The services portion of the code will build on those basic commands and provide user-facing interactions and functionality.

Example Usage

```clojure
user=> (go)
user=> (require '[eval.services.cmr.search :refer [search]])
user=> (search (context) :prod :collection {:provider "FOO"} {:format :umm-json})
```

while these two functions will perform equivalent actions, the service version is designed to be used from within the context of the running system.

## Common development tasks

| Task                  | Command                   |
|-----------------------|---------------------------|
| Download dependencies | `clj -Spath`              |
| Start the REPL        | `clj -M:dev`          |
| Run unit tests        | `clj -M:test/unit`        |
| Run feature tests     | `clj -M:test/features`    |
| Run the project       | `clj -M -m eval.system`   |
| Check deps            | `clj -X:project/outdated` |

## Common REPL commands

| Task                              | Command           |
|-----------------------------------|-------------------|
| Start all components              | `(user/go)`       |
| Stop all components               | `(user/halt)`     |
| Reset and reload all components   | `(user/reset)`    |
| Reset all components              | `(user/reset-all` |
| Return the current system context | `(user/context)`  |


## Building

```bash
clj -T:build clean
clj -T:build jar
```

## Developent

Using Polylith

```bash
clojure -M:poly info
```
