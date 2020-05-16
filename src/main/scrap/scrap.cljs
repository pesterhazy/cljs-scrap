(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [reagent.dom :as rdom]
            ["react" :as React]
            [scrap.dijkstra]))

(js/console.log (.-version React))

(defn <root>
  []
  [:div
   [:h1 "Hello"]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^:export main []
  (rdom/render [<root>] (js/document.getElementById "app")))
