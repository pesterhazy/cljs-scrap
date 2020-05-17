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

(defonce !text (recoil/atom
                #js{:key (str ::text)
                    :default "x"}))

(defonce !upper (recoil/selector
                 #js{:key (str ::upper)
                     :get (fn [ctx]
                            (str/upper-case (.get ctx !text)))}))

(defonce !person (recoil/selector
                  #js{:keys (str ::person)
                      :get (fn [ctx]
                             (-> (js/Promise. (fn [resolve]
                                                (js/setTimeout resolve 2000)))
                                 (.then (fn [] (js/fetch "https://yesno.wtf/api")))
                                 (.then (fn [result] (.json result)))
                                 (.then (fn [result]
                                          #pp result))))}))

(defn <text> []
  (let [text (recoil/useRecoilValue !text)]
    [:div "Text: " (pr-str text)]))

(defn <person> []
  (js/console.log "<person>")
  (let [loadable (recoil/useRecoilValueLoadable !person)]
    (case (.-state #pp loadable)
      "loading"
      [:div "loading..."]
      "hasValue"
      [:div "Person: " (pr-str (.-contents loadable))])))

#_(defn <wrap> []
    [:> react/Suspense {:fallback (r/as-element [:div "..."])}
     [:f> <person>]])

(defn <main> []
  (let [set-text (recoil/useSetRecoilState !text)]
    [:div
     [:h1 "welcome"]
     [:f> <person>]
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
