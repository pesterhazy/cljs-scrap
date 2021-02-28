(ns scrap.game-test
  (:require [clojure.test :as t]
            [scrap.game :as g]))

(t/deftest basics
  (let [game (g/make-game)
        roller (g/cheat-roller (cycle [5 4 3 2 1]))
        _ (t/is (thrown-with-msg? :default
                                  #"Not started"
                                  (g/roll game roller))
                "should throw")
        _ (t/is (= false (g/can-roll? game)) "Should not be able to roll")
        game (g/reset game)
        _ (t/is (= true (g/can-roll? game)))
        _ (t/is (= (repeat 5 nil) (g/die-vals game)))
        game (g/roll game roller)
        _ (t/is (= [5 4 3 2 1] (g/die-vals game)))]))

(t/deftest three-times
  (let [game (g/reset (g/make-game))
        _ (t/is (= true (g/can-roll? game)))
        roller (g/cheat-roller (cycle [5 4 3 2 1]))
        game (nth (iterate (fn [game] (g/roll game roller)) game) 3)
        _ (t/is (= false (g/can-roll? game)))
        _ (t/is (thrown-with-msg? :default
                                  #"Ran out of rolls"
                                  (g/roll game roller))
                "should throw")]))
