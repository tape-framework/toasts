(ns tape.toasts.controller
  "Toasts controller module."
  (:require
   [medley.core :as m]
   [tape.mvc.controller :as c :include-macros true]
   [tape.tools.timeouts.controller :as timeouts.c]))

;;; Model

(defn make [icon message key]
  {:icon icon, :message message, :open true, :key key})

;;; Events Utils

(def ^:private keep-toast-ms 5000)

(defn- timeout [toast]
  {:ms      keep-toast-ms
   :set     [::assoc-timeout-id toast]
   :timeout [::delete toast]})

(defn- next-key [toasts]
  (let [keys (keys toasts)
        mx   (apply max keys)]
    (inc (or mx 0))))

;;; Events

(defn create
  {::c/reg ::c/event-fx}
  [{:keys [db]} [_ icon message]]
  (let [key   (next-key (::toasts db))
        toast (make icon message key)]
    {:db              (assoc-in db [::toasts key] toast)
     ::timeouts.c/set (timeout toast)}))

(defn assoc-timeout-id
  {::c/reg ::c/event-db}
  [db [_ toast timeout-id]]
  (m/update-existing-in db [::toasts (:key toast)]
                        assoc :timeout-id timeout-id))

(defn set-timeout
  {::c/reg ::c/event-fx}
  [_cofx [_ toast]]
  (when-not (some? (:timeout-id toast))
    {::timeouts.c/set (timeout toast)}))

(defn clear-timeout
  {::c/reg ::c/event-fx}
  [{:keys [db]} [_ toast]]
  (when-let [timeout-id (:timeout-id toast)]
    {::timeouts.c/clear timeout-id
     :db                (m/update-existing-in db [::toasts (:key toast)]
                                              dissoc :timeout-id)}))

(defn delete
  {::c/reg ::c/event-fx}
  [{:keys [db]} [_ toast]]
  (let [{:keys [key timeout-id]} toast]
    (cond-> {:db (m/dissoc-in db [::toasts key])}
            (some? timeout-id) (assoc ::timeouts.c/clear timeout-id))))

;;; Subs

(defn toasts
  {::c/reg ::c/sub}
  [db _]
  (::toasts db))

;;; Module

(c/defmodule)
