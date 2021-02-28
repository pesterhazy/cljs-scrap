(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [reagent.core :as r]))

(defn <die> [n]
  [:img.die {:src (str "assets/" n ".png")}])

(defn <root>
  []
  [:div
   [<die> 1]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^:export main []
  (r/render [<root>] (js/document.getElementById "app"))

  #_(t/run-tests 'scrap.dijkstra))
