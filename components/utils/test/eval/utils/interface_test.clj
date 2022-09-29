(ns eval.utils.interface-test
  (:require
   [clojure.data.xml :as xml]
   [clojure.string :as str]
   [clojure.test :refer [deftest testing is]]
   [eval.utils.interface :as utils]))

(deftest edn->json--happy-path
  (testing "Given an EDN map"
    (let [edn {:foo "bar"}]
      (testing "When I convert the EDN to JSON"
        (is (= "{\"foo\":\"bar\"}" (utils/edn->json edn))
            "Then the EDN is converted to JSON correctly")))))

(deftest remove-nil-keys--map-with-nil-values--nils-removed
  (is (= {:blank ""
          :boolean-true true
          :boolean-false false
          :number-zero 0}
         (utils/remove-nil-keys {:nil nil
                                 :blank ""
                                 :boolean-true true
                                 :boolean-false false
                                 :number-zero 0}))))

(deftest remove-blank-keys--map-with-nil-values--nils-removed
  (is (= {:boolean-true true
          :boolean-false false
          :number-zero 0}
         (utils/remove-blank-keys {:nil nil
                                   :blank ""
                                   :boolean-true true
                                   :boolean-false false
                                   :number-zero 0}))))

(def xml-simple "
<?xml version= \"1.0\" encoding= \"UTF-8\"?>
<orders type=\"array\">
  <order>
    <order_price>test</order_price>
    <provider_orders type=\"array\">
      <provider_orders>
        <reference>
          <id>test</id>
        </reference>
      </provider_orders>
    </provider_orders>
  </order>
</orders>")

(deftest format-edn-as-xml-test--edn-with-array--formats-correctly
  (let [data [{:order_price "test"
               :provider_orders [{:reference {:id "test"}}]}]]
    (is (= (-> xml-simple
               str/trim
               xml/parse-str)
           (->> data
                (utils/edn->xml "order")
                xml/indent-str
                xml/parse-str)))))

(def ^:private xml-nested "
<?xml version=\"1.0\" encoding= \"UTF-8 \"?>
<orders type= \"array\">
  <order>
    <order_price>test</order_price>
    <nested1>
      <nested2>
        <nested3s type=\"array\">
          <nested3>
            <id>data</id>
          </nested3>
        </nested3s>
      </nested2>
    </nested1>
    <provider_orders type=\"array\">
      <provider_orders>
        <reference>
          <id>test</id>
        </reference>
      </provider_orders>
    </provider_orders>
  </order>
</orders>")

(deftest format-edn-as-xml-test--edn-with-nested-array--formats-correctly
  (let [xml-doc (-> xml-nested
                    str/trim
                    xml/parse-str)]
    (is (= xml-doc (-> (utils/edn->xml "order" [{:order_price "test"
                                                :nested1 {:nested2 {:nested3 [{:id "data"}]}}
                                                 :provider_orders [{:reference {:id "test"}}]}])
                       xml/indent-str
                       xml/parse-str)))))

(deftest map-to-test
  (is (= [[:a "a"]]
         (utils/maps-to {:a "value" :b "othervalue"}
                        {"a" "value" "b" "different"}))))
