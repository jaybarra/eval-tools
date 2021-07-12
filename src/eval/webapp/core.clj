(ns eval.webapp.core
  (:require
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-js include-css]]
   [hiccup.element :refer [link-to]]))

(defn layout
  [& elements]
  (html5
   {:lang "en"}
   [:head
    (include-css "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css")]

   [:body elements]))
