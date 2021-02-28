(ns scrap.game)

(defn make-game []
  {:die-vals (vec (repeat 6 nil))})

(defn die-vals [game]
  (:die-vals game))

(defn roll [game roll-fn]
  (assoc game :die-vals (->> (repeatedly roll-fn)
                             (take 6)
                             vec)))

(defn rand-roller []
  (fn [] (inc (rand-int 6))))
