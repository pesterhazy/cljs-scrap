(ns scrap.scrap
  (:require-macros [scrap.ppp])
  (:require [clojure.test :as t]
            [clojure.pprint]
            [clojure.string :as str]
            ["react" :as react :refer [useRef useEffect]]
            ["react-dom" :as react-dom]
            ["recoil" :as recoil]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            ["@use-it/interval" :as use-interval]
            [scrap.dijkstra]))

(defonce !text (recoil/atom #js{:key "text"
                                :default "x"}))

(defn <text> []
  (let [text (recoil/useRecoilValue !text)]
    [:div "Text: " (pr-str text)]))

(defn <main> []
  (let [set-text (recoil/useSetRecoilState !text)]
    [:div
     [:h1 "welcome"]
     [:f> <text>]
     [:button {:style {:margin-top 10}
               :on-click (fn [] (set-text (fn [s] (str s "x"))))}
      "more"]]))

(defn <root> []
  [:> recoil/RecoilRoot
   [:f> <main>]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ^:export main []
  (rdom/render [:f> <root>] (js/document.getElementById "app")))
