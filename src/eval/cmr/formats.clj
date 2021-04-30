(ns eval.cmr.formats
  (:require
   [muuntaja.core :as muuntaja]
   [muuntaja.format.core :as fmt-core]
   [muuntaja.format.json :as json-format]))

(def vnd-nasa-cmr-umm-json-format
  {:decoder [json-format/decoder {:decode-key-fn true}]
   :matches #"^application/vnd\.nasa\.cmr\.umm_results\+json.*"})

(def echo10+xml-format
  {:decoder (reify
              fmt-core/Decode
              (decode [_ xml _charset]
                xml))
   :matches #"^application/echo10\+xml.*"})
