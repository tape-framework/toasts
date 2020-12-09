(ns tape.toasts.controller
  "Toasts controller module."
  {:tape.mvc.controller/interceptors [inject]}
  (:refer-clojure :exclude [set])
  (:require
   [medley.core :as m]
   [reagent.core :as r]
   [re-frame.cofx :as cofx]
   [tape.mvc.controller :as c :include-macros true]
   [tape.tools.timeouts.controller :as timeouts.c]))

;;; Model

(defn make [icon message key]
  {:icon icon, :message message, :open true, :key key})

;;; Toasts DB

(defonce db (r/atom {}))

(defn signal
  ([_] db)
  ([_ _] db))

;;; Cofx

(defn ^{::c/cofx ::db} add [m] (assoc m ::db @db))

(def inject (cofx/inject-cofx ::db))

;;; Fx

(defn ^{::c/fx ::db} set [m] (reset! db m))

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

(defn ^::c/event-fx create
  [{::keys [db]} [_ icon message]]
  (let [key   (next-key db)
        toast (make icon message key)]
    {::db             (assoc db key toast)
     ::timeouts.c/set (timeout toast)}))

(defn ^::c/event-fx assoc-timeout-id
  [{::keys [db]}
   [_ toast timeout-id]]
  {::db (m/update-existing db (:key toast)
                           assoc :timeout-id timeout-id)})

(defn ^::c/event-fx set-timeout
  [_cofx [_ toast]]
  (when-not (some? (:timeout-id toast))
    {::timeouts.c/set (timeout toast)}))

(defn ^::c/event-fx clear-timeout
  [{::keys [db]} [_ toast]]
  (when-let [timeout-id (:timeout-id toast)]
    {::timeouts.c/clear timeout-id
     ::db               (m/update-existing db (:key toast)
                                           dissoc :timeout-id)}))

(defn ^::c/event-fx delete
  [{::keys [db]} [_ toast]]
  (let [{:keys [key timeout-id]} toast]
    (cond-> {::db (dissoc db key)}
            (some? timeout-id) (assoc ::timeouts.c/clear timeout-id))))

;;; Subs

(defn toasts
  {::c/sub     true
   ::c/signals [signal]}
  [db _] db)

;;; Module

(c/defmodule)
