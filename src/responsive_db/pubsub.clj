(ns responsive-db.pubsub
  (:require [clojure.core.async :as a]))

(defn exchange []
  (atom #{}))

(defn subscribe [exchange]
  (let [channel (a/chan)]
    (swap! exchange conj channel)
    channel))

(defn unsubscribe [exchange channel]
  (swap! exchange disj channel))

(defn publish [exchange message]
  (doseq [channel @exchange]
    (a/put! channel message)))

