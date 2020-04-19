(ns scrap.scrap)

(defn make-world [n]
  {:n n
   :!critical (atom nil)
   :!state (atom {:k -1
                  :b (vec (repeat n true))
                  :c (vec (repeat n true))})})

(defn write+ [{:keys [!state]} path v]
  (-> (js/Promise.resolve)
      ;; TODO: delay?
      (.then (fn []
               (swap! !state assoc-in path v)))))

(defn read+ [{:keys [!state]} path]
  (-> (js/Promise.resolve)
      ;; TODO: delay?
      (.then (fn []
               (get-in @!state path)))))

(defn critical+ [{:keys [n] :as world} fun+ i rc]
  (-> (js/Promise.resolve)
      (.then (fn []
               (when (> rc 10)
                 (throw (js/Error. "Recurse count > 10")))))
      (.then (fn []
               (write+ world [:b i] false)))
      (.then (fn []
               (read+ world [:k])))
      (.then (fn [k]
               (if (not= k i)
                 (-> (write+ world [:c i] true)
                     (.then (fn []
                              (read+ world [:b k])))
                     (.then (fn [b-k]
                              (-> (js/Promise.resolve
                                   (when b-k (write+ world [:k] i)))
                                  (.then (fn []
                                           (prn [::fail1])
                                           (critical+ world fun+ i (inc rc))))))))
                 (-> (write+ world [:c i] false)
                     (.then (fn []
                              (->> (range n)
                                   (reduce
                                    (fn [p j]
                                      (-> p
                                          (.then
                                           (fn [break?]
                                             (read+ world [:c j])
                                             (.then (fn [c-j]
                                                      (and break? (not= j i) (not c-j))))))))
                                    (js/Promise.resolve false)))))
                     (.then (fn [break?]
                              (if break?
                                (do
                                  (prn [::fail2])
                                  (critical+ world fun+ i (inc rc)))
                                (-> (js/Promise.resolve (fun+))
                                    (.then (fn []
                                             (js/Promise.all [(write+ world [:c i] true)
                                                              (write+ world [:b i] true)])))))))))))))

(defn process+ [{:keys [!critical]} id]
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
        world (make-world n)]
    (-> (->> (range n)
             (map (fn [i] (critical+ world #(process+ world i) i 0)))
             js/Promise.all)
        (.then (fn []
                 (prn [::done]))))))
