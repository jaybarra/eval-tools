(ns eval.elastic.core
  (:require
   [clj-http.client :as client]
   [jsonista.core :as json]
   [slingshot.slingshot :refer [try+ throw+]]
   [taoensso.timbre :as log]))

(defn create-index-template
  [conn label template]
  (let [url (format "%s/_index_template/%s" (:url conn) label)]
    (client/put url {:headers {:content-type "application/json"}
                     :body (json/write-value-as-string template)})))

(defn create-index
  [conn label index]
  (let [url (format "%s/%s" (:url conn) label)
        req (merge {:headers {:content-type "application/json"}}
                   (when index {:body (json/write-value-as-string index)}))]
    (try+
     (client/put url req) 
     (log/info "Created index [" label "]")
     {:index label}
     (catch [:status 400] {:keys [body]}
       (let [msg (json/read-value body json/keyword-keys-object-mapper)
             err-type (-> msg
                          :error
                          :root_cause
                          first
                          :type)]
         (log/warn "Attempted to created an index that already exists [" label "]")
         (if (= err-type "resource_already_exists_exception")
           {:index label}
           (throw+)))))))

(defn close-index
  [_conn _index]
  (throw (ex-info "Not yet implemented" {})))

(defn delete-index
  [conn index]
  (let [url (format "%s/%s" (:url conn) index)]
    (try+
     (client/delete url)
     {:index index}
     (catch [:status 404] _
       (log/warn "Index not found [" index "]"))
     (catch Object _
       (log/error "Unexpected error deleting index" index)
       (throw+)))))

(defn create-document
  [conn index doc id]
  (let [url (format "%s/%s/_doc" (:url conn) (name index))
        url (if id (str url "/" id) url)
        request {:headers {:content-type "application/json"}
                 :body (json/write-value-as-string doc)}
        resp (try+
              (if id
                (client/put url request)
                (client/post url request))
              (catch [:status 404] _
                (log/error "Could not add document to missing index [" index "]")
                (throw+))
              (catch Object {:keys [body]}
                (log/error "An unexpected error occurred" body)
                (throw+)))]
    (log/info (format "Indexed document [ %s ] [ %s ]"
                      index
                      (get-in resp [:headers "Location"])))
    {:index index
     :doc doc}))
