(ns scrap.game)

(defn make-game []
  {:die-vals (vec (repeat 6 nil))})

(defn die-vals [game]
  (:die-vals game))

(defn roll [game]
  (assoc game :die-vals (->> (repeatedly (fn [] (inc (rand-int 6))))
                             (take 6)
                             vec)))
