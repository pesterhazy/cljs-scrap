(ns scrap.game
  (:require [clojure.pprint]))

(def empty-game
  {:started? false
   :die-vals (vec (repeat 5 nil))
   :n-rolls 0})

(defn make-game []
  empty-game)

(defn reset [_]
  (assoc empty-game :started? true))

(defn die-vals [game]
  (:die-vals game))

(defn can-roll? [game]
  (and (:started? game) (< (:n-rolls game) 3)))

(defn roll [game roll-fn]
  (when-not (:started? game)
    (throw (ex-info "Not started" {})))
  (when (>= (:n-rolls game) 3)
    (throw (ex-info "Ran out of rolls" {})))
  (-> game
      (assoc :die-vals (vec (repeatedly 5 roll-fn)))
      (update :n-rolls inc)))

(defn rand-roller []
  (fn [] (inc (rand-int 6))))

(defn cheat-roller [die-vals]
  (let [!die-vals (atom die-vals)]
    (fn []
      (when (empty? @!die-vals)
        (throw "Ran out of dice"))
      (let [die-val (first @!die-vals)]
        (swap! !die-vals rest)
        die-val))))
