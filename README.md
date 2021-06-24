<p align="center">
  <a href="https://github.com/jaybarra/eval-tools/actions/workflows/ci-test.yml">
    <img alt="Build Status"
         src="https://img.shields.io/github/workflow/status/jaybarra/eval-tools/ci-test">
  </a>

  <a href="https://codecov.io/gh/jaybarra/eval-tools">
    <img alt="Code Coverage Report"
         src="https://codecov.io/gh/jaybarra/eval-tools/branch/core/graph/badge.svg?token=IUCQG02UCE"/>
  </a>
</p>

# Eval Tools

A Clojure library designed to assist with evalutating and testing REST
APIs and tools.

* [Documentation](https://jaybarra.github.io/eval-tools/)

## Approach

This tool is designed to interact with external APIs, with simplified commands and clients to invoke those commands. The approach to the commands is to be low-level and involve only basic query and response communication with no higher logic applied. All commands should be stateless.

```clojure
user=> (require '[eval.cmr.core :as cmr])
user=> (require '[eval.cmr.search :as cmr-search])
user=> (def my-client (cmr/create-client {:id :foo :url "http://instance"}))
user=> (def query (cmr-search/search :collection {:provider "FOO"} {:format :umm-json})))
user=> (cmr/decode-cmr-response-body
        (cmr/invoke my-client query))
```

The services portion of the code will build on those basic commands and provide user-facing interactions and functionality.

Example Usage

```clojure
user=> (go)
user=> (require '[eval.services.cmr.search :as search-svc])
user=> (require '[eval.cmr.search :as search-api])
user=> (def query (search-api/search :collection {:provider "FOO"} {:format :umm-json})))
user=> (search-svc/search (context) :prod query)
```

while these two functions will perform equivalent actions, the service version is designed to be used from within the context of the running system.

## Common development tasks

| Task                  | Command                  |
|-----------------------|--------------------------|
| Download dependencies | `clj -Spath`             |
| Start the REPL        | `clj -M:dev:nrepl`       |
| Run tests             | `clj -M:test/runner`     |
| Run the project       | `clj -M -m eval.system`  |
| Package application   | `clj -X:project/uberjar` |

## Common REPL commands

| Task                              | Command           |
|-----------------------------------|-------------------|
| Start all components              | `(user/go)`       |
| Stop all components               | `(user/halt)`     |
| Reset and reload all components   | `(user/reset)`    |
| Reset all components              | `(user/reset-all` |
| Return the current system context | `(user/context)`  |

### Emacs + Cider

Create or override your `.dir-locals.el` file and run cider from Emacs

```cl
((clojure-mode . ((cider-preferred-build-tool . clojure-cli)
                  (cider-clojure-cli-aliases . ":dev:test"))))
```

## License

Copyright © 2021 Jay Barra

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
