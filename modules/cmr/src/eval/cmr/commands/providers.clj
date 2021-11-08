(ns eval.cmr.commands.providers
  (:require
   [clojure.spec.alpha :as spec]))

(spec/def ::provider-id string?)
(spec/def ::short-name string?)
(spec/def ::cmr-only boolean?)
(spec/def ::small boolean?)
(spec/def ::provider (spec/keys :req-un [::provider-id
                                         ::short-name
                                         ::cmr-only
                                         ::small]))
(defn get-providers
  []
  {:request
   {:method :get
    :url "/metadata-db/providers"}})

(defn create-provider
  "Returns a command to create a provider"
  [provider & [opts]]
  (when-not (spec/valid? ::provider provider)
    (throw (ex-info "Invalid provider definition"
                    (spec/explain-data ::provider provider))))
  (let [command {:request
                 {:method :post
                  :url "/metadata-db/providers"
                  :headers {:content-type "application/json"}
                  :body provider}}]
    (if opts
      (assoc command :opts opts)
      command)))
