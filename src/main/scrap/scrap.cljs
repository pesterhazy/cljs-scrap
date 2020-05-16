(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [clojure.string :as str]
            ["react" :as react :refer [useRef useEffect]]
            ["react-dom" :as react-dom]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            ["@use-it/interval" :as use-interval]
            [scrap.dijkstra]))

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
  (rdom/render [:f> clock] (js/document.getElementById "app")))
