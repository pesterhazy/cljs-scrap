(ns scrap.game
  (:require [clojure.pprint]))

(defn make-game []
  {:die-vals (vec (repeat 6 nil))
   :n-rolls 0})

(defn die-vals [game]
  (:die-vals game))

(defn roll [game roll-fn]
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
