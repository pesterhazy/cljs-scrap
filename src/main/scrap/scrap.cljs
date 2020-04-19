(ns scrap.scrap)

(defn make-world [n]
  (let [state {:k 0
               :b (vec (repeat n true))
               :c (vec (repeat n true))}]
    (prn state)
    {:n n
     :!critical (atom nil)
     :!state (atom state)}))

(defn write+ [{:keys [!state]} path v]
  (-> (js/Promise.resolve)
      ;; TODO: delay?
      (.then (fn []
               (swap! !state assoc-in path v)))))

(defn read+ [{:keys [!state]} path]
  (-> (js/Promise.resolve)
      ;; TODO: delay?
      (.then (fn []
               #_(prn 'read path '=> (get-in @!state path))
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
                                           (critical+ world fun+ i (inc rc))))))))
                 (-> (write+ world [:c i] false)
                     (.then (fn []
                              (-> (->> (range n)
                                       (map (fn [j]
                                              (if (= j i)
                                                true
                                                (read+ world [:c j]))))
                                       (js/Promise.all))
                                  (.then (fn [bools]
                                           (every? identity bools))))))
                     (.then (fn [ok?]
                              (if-not ok?
                                (critical+ world fun+ i (inc rc))
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
