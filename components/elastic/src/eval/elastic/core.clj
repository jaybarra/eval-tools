(ns eval.elastic.core
  (:require
   [clojure.string :as str]
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
  [conn label index _opts]
  (let [url (format "%s/%s" (:url conn) label)
        req (merge {:headers {:content-type "application/json"}}
                   (when index {:body (json/write-value-as-string index)}))
        exists? (not= 404 (:status (client/get url {:throw-exceptions false})))]
    (try+
     (when-not exists?
       (client/put url req)
       (log/info "Created index [" label "]"))
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
       (log/warn "Index not found and may have already been deleted [" index "]"))
     (catch Object _
       (log/error "Unexpected error deleting index" index)
       (throw+)))))

(defn index-document
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
                (log/error (format "An unexpected error occurred indexing document to index [ %s ]" index) body)
                (throw+)))]
    (log/info (format "Indexed document [ %s ] [ %s ]"
                      index
                      (get-in resp [:headers "Location"])))
    {:index index
     :doc doc}))

(defn bulk-index
  [conn index docs opts]
  (let [{:keys [id-field]} opts
        url (format "%s/_bulk" (:url conn))
        payload (for [doc docs]
                  (str
                   (json/write-value-as-string
                    {:index (merge {:_index index}
                                   (when-let [id (get doc id-field)] {:_id id}))})
                   "\n"
                   (json/write-value-as-string doc)))
        payload (str (str/join "\n" payload) "\n")
        request {:headers {:content-type "application/json"}
                 :body payload}]
    (try+
     (client/post url request)
     (log/info (format "Bulk indexed [ %d ] documents into index [ %s ]"
                       (count docs)
                       index))
     {:index index}
     (catch Object {:keys [body]}
       (log/error (format "An unexpected error occurred during bulk indexing to index [ %s ]" index)
                  body)
       (throw+)))))
