(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [reagent.core :as r]))

;; TODO: remove unnecessary files

(defn <die>
  [die-val]
  [:img.die
   {:src (str "assets/" (if (nil? die-val) "question" die-val) ".png")}])

(defn <die-set> [die-vals]
  (->> die-vals
       (map (fn [die-val] [<die> die-val]))
       (into [:div])))

(defn <root>
  []
  [:div
   [:div
    [<die-set> (repeat 6 nil)]]
   [:div.menu
    [:a.menu-item.button "Start"]
    [:a.menu-item.button "Roll"]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^:export main []
  (r/render [<root>] (js/document.getElementById "app"))

  #_(t/run-tests 'scrap.dijkstra))
