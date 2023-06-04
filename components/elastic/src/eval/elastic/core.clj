(ns eval.elastic.core
  (:require
   [clojure.core.async :as async]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [org.httpkit.client :as http]
   [jsonista.core :as json]))

(defn create-index-template
  "Send a command to Elasticsearch to create an index-template."
  [conn label template]
  (let [url (format "%s/_index_template/%s" (:url conn) label)]
    (http/put url {:headers {:content-type "application/json"}
                   :body (json/write-value-as-string template)})))

(defn create-index
  "Send a command to Elasticsearch to create an index with name LABEL as defined by the INDEX."
  [conn label index]
  (let [url (format "%s/%s" (:url conn) label)
        req {:headers {"content-type" "application/json"}
             :body (json/write-value-as-string index)}]
    (http/put
     url req (fn [{:keys [status headers body error]}]
               (if error
                 (throw (ex-info "Failed to transmit to Elasticsearch"
                                 {:action :create-index
                                  :label label
                                  :index-definition index} error))
                 (if (= 200 status)
                   [:ok label]
                   (let [json-body (json/read-value body json/keyword-keys-object-mapper)
                         es-err-type (-> json-body
                                         :error
                                         :root_cause
                                         first
                                         :type)]
                     (case es-err-type
                       "resource_already_exists_exception"
                       (do (log/warn "Attempted to create an index that already exists [" label "]")
                           [:ok label])
                       ;; default
                       (do (log/error "Creating the Elasticsearch index failed")
                           [:error label])))))))))
(comment
  @(create-index {:url "http://localhost:9200"}
                 "collections-00001"
                 {:settings {:index {:number_of_shards 3 :number_of_replicas 2}}
                  :mappings {:properties {:title {:type :text}
                                          :full-text {:type :text}}}})
  @(delete-index {:url "http://localhost:9200"}
                 "collections-00001"))

(defn close-index
  [_conn _index]
  (throw (ex-info "Not yet implemented" {})))

(defn delete-index
  [conn label]
  (let [url (format "%s/%s" (:url conn) label)]
    (http/delete url (fn [{:keys [status headers body error]}]
                       (condp >= status
                         200 [:ok label]
                         401 [:error label]
                         404 [:ok label])))))

(defn index-document
  [conn index doc id]
  (let [url (format "%s/%s/_doc" (:url conn) (name index))
        url (if id (str url "/" id) url)
        request {:headers {:content-type "application/json"}
                 :body (json/write-value-as-string doc)}
        resp (if id
               (http/put url request)
               (http/post url request))]
    (log/info (format "Indexed document [ %s ] [ %s ]"
                      index
                      (get-in resp [:headers "Location"])))
    {:index index
     :doc doc}))

(defn bulk-index
  "Bulk indexing capability for Elasticsearch.
  This function is restricted to indexing operations."
  [conn index docs opts]
  (let [{:keys [id-field]} opts
        url (format "%s/_bulk" (:url conn))
        payload (for [doc docs]
                  (str
                   (json/write-value-as-string
                    {:index (merge {:_index index}
                                   (when-let [id (get doc id-field)]
                                     {:_id id}))})
                   "\n"
                   (json/write-value-as-string doc)))
        payload (str (str/join "\n" payload) "\n")
        request {:headers {:content-type "application/json"}
                 :body payload}]
    (http/post url request)
    (log/info (format "Bulk indexed [ %d ] documents into index [ %s ]"
                      (count docs)
                      index))
    {:index index}))

(defn delete-by-query
  [conn index query _]
  (let [url (format "%s/%s/_delete_by_query" (:url conn) index)
        request {:headers {:content-type "application/json"}
                 :body (json/write-value-as-string query)}]
    (log/info (format "Ran _delete_by_query against [ %s ]" index))
    (http/post url {:callback (fn [err res]
                                (if err
                                  [:error err]
                                  [:ok res]))})))

(comment
  (let [response @(http/get "https://cmr.uat.earthdata.nasa.gov/access-control/permissions/jay.barra.sit")]

    (println (:body response)))
  )
