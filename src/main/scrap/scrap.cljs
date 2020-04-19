(ns scrap.scrap)

(defn make-world []
  {:!critical (atom nil)})


#_(defn write+ [world path v]
    (-> (js/Promise.reolve)
        ;; TODO: delay?
        (.then (fn []
                 (swap! (:!state o) assoc-in path v)))))

(defn banana+ [{:keys [!critical]}]
  (-> (js/Promise.resolve)
      (.then (fn []
               (when @!critical
                 (throw (js/Error. "Another process is already in critical section")))
               (prn [::enter])
               (reset! !critical true)))
      (.then (fn []
               ;; wait?
               ))
      (.then (fn []
               (prn [::leave])
               (reset! !critical false)))))

(defn ^:export main []
  (-> (banana+ (make-world))
      (.then (fn []
               (prn :ok)))))
