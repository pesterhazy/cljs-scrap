(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [clojure.string :as str]
            ["react" :as react]
            ["react-dom" :as react-dom]
            ["@use-it/interval" :as interval]
            [scrap.dijkstra]))

(js/console.log (.-version react))

(defn use-interval [callback delay]
  (let [saved-callback (react/useRef)]
    (react/useEffect
     (fn []
       #_(js/console.log "new callback" callback)
       (set! (.-current saved-callback) callback))
     #js [callback])
    (react/useEffect
     (fn []
       #_(js/console.log "new delay" delay)
       (let [tick (fn []
                    (js/console.log "tick")
                    (.current saved-callback))]
         (when delay
           (js/console.log "setInterval")
           (let [id (js/setInterval tick delay)]
             (fn []
               #_(js/console.log "clearInterval")
               (js/clearInterval id))))))
     #js [delay])))

(defn clock []
  (js/console.log "clock")
  (let [[cnt set-cnt] (react/useState (js/Date.now))]
    (interval (fn []
                (let [new-cnt (js/Date.now)]
                  (js/console.log "new-cnt" new-cnt)
                  (set-cnt new-cnt)))
              1000)
    (react/createElement "div"
                         nil
                         cnt)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ^:export main []
  (react-dom/render (react/createElement clock nil nil)
                    (js/document.getElementById "app")))
