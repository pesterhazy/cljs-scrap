(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [reagent.core :as r]
            [scrap.dijkstra]))

(defn <root> []
  [:div
   [:h1 "Hello"]
   [:div "world"]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^:export main []
  (r/render [<root>] (js/document.getElementById "app"))

  #_(t/run-tests 'scrap.dijkstra))
