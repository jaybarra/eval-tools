(ns eval.core
  (:require
   [ajax.core :refer [GET]]
   [reagent.core :as reagent]
   [reagent.dom :as reagent-dom]))

(defonce state (reagent/atom
                {:health "Pending"
                 :active-cmr :local
                 :collections []}))

(GET "http://localhost:8880/api/health"
     {:handler       (fn [res] (swap! state assoc :health res))
      :error-handler (fn [_] (swap! state assoc :health "Failed Connection"))})

(GET "http://localhost:8880/api/cmr/local/providers"
     {:handler       (fn [res] (swap! state update :local assoc :providers res))
      :error-handler (fn [_] (swap! state update :local dissoc :providers))})

(GET "http://localhost:3003/collections.umm_json"
     {:handler       (fn [res] (swap! state update :local assoc :collections (:items res)))
      :error-handler (fn [_] (swap! state update :local dissoc :collections))})

(defn provider-card [{short-name :short-name
                      provider-id :provider-id}]
  [:div.card {:key provider-id}
   [:div.card-content
    [:div.media-content
     [:p.title short-name]]]])

(defn collection-card [{_ :umm
                        {concept-id :concept-id
                         entry-title :entry-title} :meta}]
  [:div.card {:key concept-id}
   [:div.card-content
    [:div.media-content
     [:p.title entry-title]]]])

(def header
  [:section.hero.is-primary
   [:div.hero-head
    [:nav.navbar
     [:div.container
      [:div.navbar-brand
       [:a.navbar-item "Eval Tools"]]
      [:div.navbar-item
       [:span "CMR-Instance "]
       [:strong#active-cmr (get @state :active-cmr)]]]]]])

(defn index-page []
  [:div

   header

   [:section.section
    [:h2.title "Providers"]
    (for [provider (get-in @state [(get @state :active-cmr) :providers])]
      (provider-card provider))]

   [:section.section
    [:h2.title "Collections"]
    (for [collection (get-in @state [(get @state :active-cmr) :collections])]
      (collection-card collection))]])

(reagent-dom/render [index-page]
                    (js/document.getElementById "root"))
