(ns eval.cmr.tag
  (:require
   [clj-http.client :as client]
   [clojure.spec.alpha :as spec]
   [eval.cmr.core :as cmr]
   [muuntaja.core :as m]
   [taoensso.timbre :as log]))

;; Specs =============================================================
(spec/def ::tag_key string?)
(spec/def ::description string?)
(spec/def ::tag (spec/keys :req [::tag_key]
                           :opt [::description]))

;; Functions =========================================================
(defn tag-by-collection-id!
  "Create a tag association by ID in CMR."
  [state tag & coll-ids]
  (let [cmr-url (::cmr/url (cmr/state->cmr state))
        echo-token (cmr/echo-token (::cmr/env (cmr/state->cmr state)))
        url (format "%s/tags/%s/associations" cmr-url tag)]
    (when coll-ids
      (log/debug (format "Tagging collection [%s] with tag [%s]"
                         coll-ids
                         tag))
      (client/post
       url
       {:body (m/encode "application/json" (map #(into {} {:concept_id %}) coll-ids))
        :headers {"Content-Type" "application/json"
                  "Echo-Token" echo-token}}))))

