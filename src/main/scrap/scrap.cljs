(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [clojure.string :as str]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            ["react" :as react]
            [scrap.dijkstra]))

(js/console.log (.-version react))

(defn clock [time-color]
  (let [[timer update-time] (react/useState (js/Date.))
        time-str (-> timer .toTimeString (str/split " ") first)]
    (react/useEffect
     (fn []
       (let [i (js/setInterval #(update-time (js/Date.)) 1000)]
         (fn []
           (js/clearInterval i)))))
    [:div.example-clock
     {:style {:color time-color}}
     time-str]))

(defn <root>
  []
  [:div
   [:h1 "Hello2"]
   [clock]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def functional-compiler (r/create-compiler {:function-components true}))

(defn ^:export main []
  (rdom/render <root> (js/document.getElementById "app") functional-compiler))
