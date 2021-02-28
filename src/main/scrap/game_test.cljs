(ns scrap.game-test
  (:require [clojure.test :as t]
            [scrap.game :as g]))

(t/deftest t
  (let [game (g/make-game)]
    (t/is (= (repeat 6 nil) (g/die-vals game)))))
