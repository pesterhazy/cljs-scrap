(ns scrap.game)

(defn make-game []
  {:die-vals (repeat 6 nil)})

(defn die-vals [game]
  (:die-vals game))
