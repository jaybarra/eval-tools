(ns eval.cmr.commands.providers
  (:require
   [clojure.spec.alpha :as spec]
   [eval.cmr.client :as cmr]))

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
  {::cmr/request
   {:method :get
    :url "/ingest/providers"}
   ::cmr/category :read})

(defn create-provider
  "Returns a command to create a provider"
  [provider & [opts]]
  (when-not (spec/valid? ::provider provider)
    (throw (ex-info "Invalid provider definition"
                    (spec/explain-data ::provider provider))))
  (let [command {::cmr/request
                 {:method :post
                  :url "/ingest/providers"
                  :headers {:content-type "application/json"}
                  :body provider}
                 ::cmr/category :create-provider}]
    (if opts
      (assoc command :opts opts)
      command)))
