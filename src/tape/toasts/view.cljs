(ns tape.toasts.view
  (:require [cljsjs.react-transition-group]
            [re-frame.core :as rf]
            [tape.mvc.view :as v :include-macros true]
            [tape.toasts.controller :as toasts.c]))

;;; Views

(defn index
  {::v/reg ::v/view}
  []
  [:> js/ReactTransitionGroup.TransitionGroup {:class "toasts"}
   (for [[key toast] @(rf/subscribe [::toasts.c/toasts])
         :let [{:keys [open icon message]} toast]]
     ^{:key key}
     [:> js/ReactTransitionGroup.CSSTransition
      {:in open :timeout 300 :class-names "toast"}
      [:div.notification.toast
       {:class          (str "is-" (name icon))
        :on-click       #(rf/dispatch [::toasts.c/delete toast])
        :on-mouse-enter #(rf/dispatch [::toasts.c/clear-timeout toast])
        :on-mouse-leave #(rf/dispatch [::toasts.c/set-timeout toast])}
       message]])])

;;; Module

(v/defmodule)
