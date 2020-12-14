(ns eval.xml
  (:import
   javax.xml.validation.SchemaFactory
   javax.xml.XMLConstants
   javax.xml.transform.stream.StreamSource
   org.xml.sax.ext.DefaultHandler2
   java.io.StringReader
   java.io.StringWriter
   org.w3c.dom.Node
   org.w3c.dom.bootstrap.DOMImplementationRegistry
   org.w3c.dom.ls.DOMImplementationLS
   org.w3c.dom.ls.LSSerializer
   org.xml.sax.InputSource
   org.xml.sax.SAXParseException
   javax.xml.parsers.DocumentBuilderFactory))

(defn pretty-print-xml
  "Returns the pretty printed xml for the given xml string"
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
