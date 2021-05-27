(ns eval.webapp.core
  (:require
   [reagent.dom :as rdom]))

;; View
(defn eval-tools-app
  []
  [:div
   [:section.evaltools
    [:div "Collections"]]]
  [:h1 "Eval Tools"])

(defn render
  []
  (rdom/render [eval-tools-app] (.getElementById js/document "app")))

(defn ^:export main
  []
  (render))

(defn ^:dev/after-load reload!
  []
  (render))
