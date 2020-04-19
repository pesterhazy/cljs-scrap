(ns scrap.scrap)

(defn make-world []
  {:!critical (atom nil)})


#_(defn write+ [world path v]
    (-> (js/Promise.reolve)
        ;; TODO: delay?
        (.then (fn []
                 (swap! (:!state o) assoc-in path v)))))

(defn banana+ [{:keys [!critical]} id]
  (-> (js/Promise.resolve)
      (.then (fn []
               (when @!critical
                 (throw (js/Error. "Another process is already in critical section")))
               (prn [::enter id])
               (reset! !critical true)))
      (.then (fn []
               ;; wait?
               ))
      (.then (fn []
               (prn [::leave id])
               (reset! !critical false)))))

(defn ^:export main []
  (let [n 2
        world (make-world)]
    (-> (->> (range n)
             (map (fn [i] (banana+ world i)))
             js/Promise.all)
        (.then (fn []
                 (prn [::done]))))))
