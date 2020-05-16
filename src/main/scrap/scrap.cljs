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

(defn use-interval [callback delay]
  (let [saved-callback (react/useRef)]
    (react/useEffect
     (fn []
       (set! (.-current saved-callback) callback))
     #js [callback])
    (react/useEffect
     (fn []
       (let [tick (fn []
                    (.current saved-callback))]
         (when delay
           (js/console.log "setInterval")
           (let [id (js/setInterval tick delay)]
             (fn []
               (js/console.log "clearInterval")
               (js/clearInterval id))))))
     #js [delay])))

(defn clock []
  (js/console.log "clock")
  (let [[timer update-time] (react/useState (js/Date.))]
    (use-interval (fn []
                    (js/console.log "callback")
                    #_(update-time (js/Date.))) 1000)
    #_(react/useEffect
       (fn []
         (js/console.log "setInterval")
         (let [i (js/setInterval #(update-time (js/Date.)) 1000)]
           (fn []
             (js/console.log "clearInterval")
             (js/clearInterval i)))))
    (r/as-element
     [:div (-> timer .toTimeString (str/split " ") first)])))

(defn <root>
  []
  (js/console.log "root")
  [:div
   [:h1 "Hello3"]
   [:> clock]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def functional-compiler (r/create-compiler {:function-components true}))

(defn ^:export main []
  (rdom/render <root> (js/document.getElementById "app") functional-compiler))
