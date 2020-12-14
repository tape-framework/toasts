(ns tape.toasts.devcards
  (:require
   [clojure.string :as str]
   [cljs.pprint :as pp]
   [integrant.core :as ig]
   [re-frame.core :as rf]
   [devcards.core :as devcards :refer-macros [defcard-rg]]
   [tape.module :as module :include-macros true]
   [tape.tools.timeouts.controller]
   [tape.toasts.controller :as toasts.c]
   [tape.toasts.view :as toasts.v]))

(module/load-hierarchy)
(def config (module/read-config "tape/toasts/config.edn"))
(def system (-> config module/prep-config ig/init))

(defcard-rg toasts
  (fn []
    (let [create (fn [k] #(rf/dispatch [::toasts.c/create k (name k)]))
          button (fn [k]
                   [:a.button {:on-click (create k)
                               :class    (str "is-" (name k) " is-fullwidth")}
                    (str/capitalize (name k))])]
      [:<>
       [:link {:rel "stylesheet" :href "/tape/toasts.css"}]
       [:div.columns
        [:div.column
         [:div.buttons
          [button :success]
          [button :danger]
          [button :warning]
          [button :info]]]
        [:div.column {:style {:overflow-x "scroll"}}
         [:pre {:style {:max-height "200px"}}
          (with-out-str (pp/pprint @(rf/subscribe [::toasts.c/toasts])))]]
        [:div.column {:style {:position "relative"}}
         [toasts.v/index]]]])))

(devcards/start-devcard-ui!)
