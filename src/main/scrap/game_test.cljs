(ns scrap.game-test
  (:require [clojure.test :as t]
            [scrap.game :as g]))

(t/deftest t
  (let [game (g/make-game)
        _ (t/is (= (repeat 6 nil) (g/die-vals game)))
        game (g/roll game)
        ;; FIXME: better test?
        _ (t/is (every? some? (g/die-vals game)))]))
