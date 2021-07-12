(ns eval.webapp.cmr.ingest
  (:require
   [eval.services.cmr.ingest :as ingest]
   [eval.webapp.core :refer [layout]]
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-js include-css]]
   [hiccup.element :refer [link-to]]
   [ring.util.response :refer [response]]))

(def page-uploads (atom {}))

(def upload-collection-page
  (layout
   [:section.hero.is-info
    [:div.hero-body
     [:div.container
      [:h1.title "Ingest"]
      [:p.subtitle "Collections"]]]]

   [:section.section
    [:form {:action "/file" :method :post :enctype "multipart/form-data"}
     [:div.file
      [:label.file-label
       [:input.file-input {:type "file" :name "collection-metadata"}]
       [:span.file-cta
        [:span.file-icon
         [:i.fas.fa-upload]]
        [:span.file-label
         "Choose a collection metadata file..."]]]]
     [:button.button.is-primary {:type "submit" :name "submit" :value "submit"} "Submit"]]]))

(defn upload-collection-success-page
  [{filename :filename size :size}]
  (layout
   [:section.here.is-info
    [:div.hero-body
     [:h1.title "Upload Successful"]
     [:p (format "%s : %d bytes" filename size)]]]))


(defn handle-collection-upload
  [{{{:keys [file]} :multipart} :parameters}]
  (response (upload-collection-success-page file)))

(def routes
  [["/file" {:handler handle-collection-upload}]
   ["" {:handler (constantly (response upload-collection-page))}]])
