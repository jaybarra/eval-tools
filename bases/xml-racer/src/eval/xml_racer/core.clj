(ns eval.xml-racer.core
  (:require
   [clojure.data.xml :as xml]
   [clojure.java.io :as io]
   [criterium.core :as criterium])
  (:import
   org.codehaus.stax2.XMLInputFactory2))

(defn woodstox
  "Convert an XML input source to Clojure using Woodstox."
  [xml-data]
  (let [factory (XMLInputFactory2/newInstance)
        stream-reader (.createXMLStreamReader factory xml-data)]
    (loop [has-next? (.hasNext stream-reader)
           xml-struct {}]
      (if has-next?
        (do
          (.next stream-reader) ;; icky stateful java
          (cond
            ;; skip blank chars
            (.isCharacters stream-reader) (recur (.hasNext stream-reader) xml-struct)
            ;; start parsing actual data
            (.isStartElement stream-reader) (recur (.hasNext stream-reader)
                                                   (assoc xml-struct (keyword (.getLocalName stream-reader)) "placeholder"))
            :else (recur (.hasNext stream-reader) xml-struct)))
        xml-struct))))

(defn clojure-xml
  "Convert an XML input source to Clojure using clojure.data.xml."
  [xml-data]
  (-> xml-data xml/parse xml-seq doall first))

(defn xml->umm
  "Convert XML to Clojure with plugable parser."
  [xml-file-path parser-fn]
  (with-open [rdr (io/input-stream xml-file-path)]
    (parser-fn rdr)))

(comment

  (def echo10-file "/Users/jbarra/workspace/cmr/cmr/system-int-test/resources/echo10-samples/collection.echo10")

  (xml->umm echo10-file #'woodstox)
  (xml->umm echo10-file #'clojure-xml)

  (criterium/quick-bench (xml->umm echo10-file #'woodstox))
  (criterium/quick-bench (xml->umm echo10-file #'clojure-xml)))
