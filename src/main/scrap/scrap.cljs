(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [reagent.core :as r]
            [scrap.dijkstra]))


(defonce !tick (r/atom 0))

(defn <clock-1> []
  (r/with-let [tick @!tick]
    [:div
     "clock:"
     tick]))

(defn <clock-2> []
  [:div
   "clock:"
   @!tick])

(defn <root>
  []
  (r/with-let [!timer (atom nil)]
    [:div {:ref (fn [e]
                  (if e
                    (reset! !timer (js/setInterval (fn []
                                                     (swap! !tick inc)
                                                     (js/console.log @!tick))
                                                   1000))
                    (js/clearInterval @!timer)))}
     [:h1 "Hello"]
     [<clock-1>]
     [<clock-2>]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^:export main []
  (r/render [<root>] (js/document.getElementById "app"))

  #_(t/run-tests 'scrap.dijkstra))
