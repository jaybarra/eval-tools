(ns eval.xml
  (:import
   java.io.StringReader
   java.io.StringWriter
   org.w3c.dom.bootstrap.DOMImplementationRegistry
   org.w3c.dom.ls.DOMImplementationLS
   org.xml.sax.InputSource
   javax.xml.parsers.DocumentBuilderFactory))

(defn pretty-print-xml
  "Returns the pretty printed xml for the given xml string. Used for
  testing different versions of Java and how they format pretty-printed
  xml strings. Seems to vary between Oracle and OpenJDK versions."
  [^String xml]
  (let [src (InputSource. (StringReader. xml))
        builder (.newDocumentBuilder (DocumentBuilderFactory/newInstance))
        document (.getDocumentElement (.parse builder src))
        keep-declaration (.startsWith xml "<?xml")
        registry (DOMImplementationRegistry/newInstance)
        ^DOMImplementationLS impl (.getDOMImplementation registry "LS")
        writer (.createLSSerializer impl)
        dom-config (.getDomConfig writer)
        output (.createLSOutput impl)]
    (.setParameter dom-config "format-pretty-print" true)
    (.setParameter dom-config "xml-declaration" keep-declaration)
    (.setCharacterStream output (new StringWriter))
    (.setEncoding output "UTF-8")
    (.write writer document output)
    (str (.getCharacterStream output))))
