(ns scrap.game-test
  (:require [clojure.test :as t]
            [scrap.game :as g]))

(t/deftest t
  (let [game (g/make-game)
        _ (t/is (= (repeat 6 nil) (g/die-vals game)))
        game (g/roll game (g/cheat-roller [5 4 3 2 1]))
        _ (t/is (= [5 4 3 2 1] (g/die-vals game)))]))
