(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [reagent.core :as r]
            [scrap.game :as g]))

(defn <die>
  [die-val]
  [:img.die
   {:src (str "assets/" (if (nil? die-val) "question" die-val) ".png")}])

(defn <die-set> [die-vals]
  (->> die-vals
       (map (fn [die-val] [<die> die-val]))
       (into [:div])))

(defonce !game (r/atom (g/make-game)))

(defn <root>
  []
  [:div
   [:div
    [<die-set> (g/die-vals @!game)]]
   [:div.menu
    [:a.menu-item.button "Start"]
    [:a.menu-item.button
     {:on-click (fn []
                  (swap! !game (fn [game] (g/roll game (g/rand-roller)))))}
     "Roll"]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ^:export main []
  (r/render [<root>] (js/document.getElementById "app")))
