# Eval Tools

<p align="center">
  <a href="https://github.com/jaybarra/eval-tools/actions/workflows/ci-test.yml">
    <img alt="Build Status"
         src="https://github.com/jaybarra/eval-tools/workflows/Run%20Unit%20Tests/badge.svg"/>
  </a>

  <a href="https://codecov.io/gh/jaybarra/eval-tools">
    <img alt="Code Coverage Report"
         src="https://codecov.io/gh/jaybarra/eval-tools/branch/core/graph/badge.svg?token=IUCQG02UCE"/>
  </a>

  <a href="https://github.com/jaybarra/eval-tools/actions/workflows/linter.yml">
    <img alt="Linter Report"
         src="https://github.com/jaybarra/eval-tools/workflows/Lint%20Code%20Base/badge.svg"/>
  </a>
</p>

A Clojure library designed to assist with evalutating and testing REST
APIs and tools.

## Approach

This tool is designed to interact with external APIs, with simplified commands and clients to invoke those commands. The approach to the commands is to be low-level and involve only basic query and response communication with no higher logic applied. All commands should be stateless.

```clojure
user=> (require '[eval.cmr.core :as cmr])
user=> (require '[eval.cmr.search :as cmr-search])
user=> (def my-client (cmr/create-client {:id :foo :url "http://instance"}))
user=> (def command (cmr-search/search :collection {:provider "FOO"} {:format :umm-json})))
user=> (cmr/decode-cmr-response-body
        (cmr/invoke my-client command))
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
| Start the REPL        | `clj -M:env/dev`          |
| Run unit tests        | `clj -M:test/unit`        |
| Run feature tests     | `clj -M:test/features`    |
| Run the project       | `clj -M -m eval.system`   |
| Check deps            | `clj -X:project/outdated` |
| Package application   | `clj -X:project/uberjar`  |

## Common REPL commands

| Task                              | Command           |
|-----------------------------------|-------------------|
| Start all components              | `(user/go)`       |
| Stop all components               | `(user/halt)`     |
| Reset and reload all components   | `(user/reset)`    |
| Reset all components              | `(user/reset-all` |
| Return the current system context | `(user/context)`  |

## Shadow-CLJS

To start the Shadow-CLJS REPL
```sh
npm install
npx shadow-cljs watch app
```

### Emacs + Cider

Create or override your `.dir-locals.el` file and run cider from Emacs

```cl
((clojure-mode . ((cider-preferred-build-tool . clojure-cli)
                  (cider-clojure-cli-aliases . "env/dev:env/cljs"))))
```
