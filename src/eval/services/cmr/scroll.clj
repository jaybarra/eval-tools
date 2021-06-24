(ns eval.services.cmr.scroll
  (:require
   [clojure.core.async :as a]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [eval.cmr.core :as cmr]
   [eval.cmr.search :as search]
   [eval.services.cmr.core :refer [context->client]]
   [eval.services.cmr.search :as s]
   [taoensso.timbre :as log]))

#_(defn fetch-granule-urs
    "Return the list of granule URs from CMR based on a query.
  And optional amount value may be specified.

  TODO: this is blocking and should have an async version"
    [context cmr-inst query & [{ch :ch :as opts}]]
    (let [client (context->client context cmr-inst)
          available (s/query-hits context cmr-inst :granule query opts)
          limit (min available (get opts :limit available))

          scroll-page (partial cmr/invoke client (/)query)

          first-page (scroll-page {:format :umm_json})
          scroll-id (:CMR-Scroll-Id first-page )
          granules (cmr/umm-json-response->items (:response first-page))
          urs (map (comp :GranuleUR :umm) granules)]
      (try
        (loop [urs urs]
          (if (>= (count urs) limit)
            urs
            (recur (->> (scroll-page {:format :umm_json
                                      :CMR-Scroll-Id scroll-id})
                        :response
                        cmr/umm-json-response->items
                        (map (comp :GranuleUR :umm))
                        (concat urs)))))
        (finally
          (cmr/invoke client (search/clear-scroll-session scroll-id))))))

#_(defn scroll-granule-urs->file!
    "Return a filename containing the list of granule URs from CMR based
  on a query. An optional amount value may be specified.

  This is suitable for granule amounts that cannot fit in memory.

  TODO: this is blocking and should have an async version

  See also: [[scroll-granule-urs]]"
    [client out-file query xf & [opts]]
    (let [available (cmr/query-hits client :granule query)
          limit (min available (get opts :limit available))

          scroll-page (partial cmr/scroll! client :granule query)

          first-page (scroll-page {:format :umm_json})
          scroll-id (:CMR-Scroll-Id first-page)
          granules (cmr/umm-json-response->items (:response first-page))
          instructions (->> granules
                            (map (comp :GranuleUR :umm))
                            (map xf))]

      (spit out-file (str/join "\n" instructions))
      (try
        (loop [scrolled (count instructions)]
          (log/debug (str scrolled " granule urs written to " out-file))
          (if (>= scrolled limit)
            (.exists (io/file out-file))
            (let [instructions (->> (scroll-page {:format :umm_json
                                                  :CMR-Scroll-Id scroll-id})
                                    :response
                                    cmr/umm-json-response->items
                                    (map (comp :GranuleUR :umm))
                                    (map xf))]
              (spit out-file (str/join "\n" instructions) :append true)
              (spit out-file "\n" :append true)
              (recur (+ scrolled (count instructions))))))
        (finally
          (cmr/clear-scroll-session! client scroll-id)))))
