(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [clojure.string :as str]
            ["react" :as react :refer [useRef useEffect]]
            ["react-dom" :as react-dom]
            [scrap.dijkstra]))

(js/console.log (.-version react))

(defn use-interval [callback delay]
  (let [saved-callback (useRef)]
    (useEffect
     (fn []
       (set! (.-current saved-callback) callback)
       js/undefined)
     #js [callback])
    (useEffect
     (fn []
       (let [handler (fn [& args]
                       (.apply (.-current saved-callback)
                               nil
                               (into-array args)))]
         (if (identical? nil delay)
           js/undefined
           (let [id (js/setInterval handler delay)]
             (fn []
               (js/clearInterval id))))))
     #js [delay])))

(defn clock []
  (js/console.log "clock")
  (let [[cnt set-cnt] (react/useState (js/Date.now))]
    ;; If you replace use-interval with my-use-interval, things break
    ;; - why?  There must be some subtle difference between
    ;; use-interval and my-use-interval but I can't see it.
    (use-interval (fn []
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
