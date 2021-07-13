(ns eval.core
  (:require
   [ajax.core :refer [GET POST]]
   [reagent.core :as reagent]
   [reagent.dom :as reagent-dom]))

(defn index-page []
  [:section.hero.is-info
   [:p.hero-body
    [:h1.title  "Eval Tools"]]])

(reagent-dom/render [index-page]
                    (js/document.getElementById "root"))
