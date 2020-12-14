(ns eval.xml-test
  (:require
   [clojure.test :refer :all]
   [eval.xml :as xml]))

(def iso-xml
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
  <gmi:MI_Metadata xmlns:gmi=\"http://www.isotc211.org/2005/gmi\"
  xmlns:gco=\"http://www.isotc211.org/2005/gco\"
  xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"
  xmlns:gmx=\"http://www.isotc211.org/2005/gmx\"
  xmlns:gsr=\"http://www.isotc211.org/2005/gsr\"
  xmlns:gss=\"http://www.isotc211.org/2005/gss\"
  xmlns:gts=\"http://www.isotc211.org/2005/gts\"
  xmlns:srv=\"http://www.isotc211.org/2005/srv\"
  xmlns:gml=\"http://www.opengis.net/gml/3.2\"
  xmlns:xlink=\"http://www.w3.org/1999/xlink\"
  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
  xmlns:swe=\"http://schemas.opengis.net/sweCommon/2.0/\"
  xmlns:eos=\"http://earthdata.nasa.gov/schema/eos\"
  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><!--Other Properties, all:0, coi:0,ii:0,si:0,pli:0,pri:0,qi:0,gi:0,ci:0,dk:0,pcc:0,icc:0,scc:0-->
  <gmd:fileIdentifier>
  <gco:CharacterString>gov.nasa.echo:DatasetID</gco:CharacterString>
  </gmd:fileIdentifier>
  </gmi:MI_Metadata>")

(def pretty-printed-iso-xml
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gmi:MI_Metadata xmlns:eos=\"http://earthdata.nasa.gov/schema/eos\"\n    xmlns:gco=\"http://www.isotc211.org/2005/gco\"\n    xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"\n    xmlns:gmi=\"http://www.isotc211.org/2005/gmi\"\n    xmlns:gml=\"http://www.opengis.net/gml/3.2\"\n    xmlns:gmx=\"http://www.isotc211.org/2005/gmx\"\n    xmlns:gsr=\"http://www.isotc211.org/2005/gsr\"\n    xmlns:gss=\"http://www.isotc211.org/2005/gss\"\n    xmlns:gts=\"http://www.isotc211.org/2005/gts\"\n    xmlns:srv=\"http://www.isotc211.org/2005/srv\"\n    xmlns:swe=\"http://schemas.opengis.net/sweCommon/2.0/\"\n    xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n    <!--Other Properties, all:0, coi:0,ii:0,si:0,pli:0,pri:0,qi:0,gi:0,ci:0,dk:0,pcc:0,icc:0,scc:0-->\n    <gmd:fileIdentifier>\n        <gco:CharacterString>gov.nasa.echo:DatasetID</gco:CharacterString>\n    </gmd:fileIdentifier>\n</gmi:MI_Metadata>\n")


(deftest pretty-print-xml-test
  (is (= pretty-printed-iso-xml (xml/pretty-print-xml iso-xml))))
