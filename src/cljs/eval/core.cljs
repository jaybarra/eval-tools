(ns eval.core
  (:require
   [ajax.core :refer [GET POST]]
   [reagent.core :as reagent]
   [reagent.dom :as reagent-dom]))

(defonce state (reagent/atom {:health "Pending"}))

(GET "http://localhost:8880/api/health"
     {:handler       (fn [res] (swap! state assoc :health (str res)))
      :error-handler (fn [res] (swap! state assoc :health "Failed Connection"))})

(GET "http://localhost:8880/api/providers"
     {:handler       (fn [res] (swap! state assoc :providers res))
      :error-handler (fn [res] (swap! state dissoc :providers))})

(defn provider-card [{short-name :short-name
                      provider-id :provider-id
                      cmr-only :cmr-only
                      small :small}]
  [:div.card {:key provider-id}
   [:div.card-content
    [:div.media-content
     [:p.title short-name]]]])

(defn index-page []
  [:div
   [:section.hero.is-info
    [:div.hero-body
     [:h1.title  "Eval Tools"]
     [:p.subtitle (get @state :health "No Connection")]]]

   [:section
    [:h2.title "Providers"]
    [:div
     (for [provider (get @state :providers)]
       (provider-card provider))]]])

(reagent-dom/render [index-page]
                    (js/document.getElementById "root"))
