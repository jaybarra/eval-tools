# Eval Tools

A Clojure library designed to assist with evalutating and testing REST
APIs and tools.

[Eval Tools Documentation](https://jaybarra.github.io/eval-tools/)

[Code](https://github.com/jaybarra/eval-tools)

## Usage

In your local environment set the following Echo token

* `CMR_ECHO_TOKEN_<env>`

e.g `CMR_ECHO_TOKEN_UAT`

### Uberjar

```sh
java -jar eval-tools.jar -m eval.system
```

### REPL

The `user` namespace provides the following commands
* (go)
* (halt)
* (reset)
* (reset-all)

## License

Copyright © 2021 Jay Barra

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
