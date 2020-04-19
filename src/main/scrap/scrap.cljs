(ns scrap.scrap)

(defn make-world [n]
  (let [state {:k 0
               :b (vec (repeat n true))
               :c (vec (repeat n true))}]
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

(defn delay+ [wait-ms]
  (js/Promise. (fn [resolve]
                 (js/setTimeout resolve wait-ms))))

(def max-recursion 100)

(defn critical+ [{:keys [n] :as world} fun+ i rc]
  (js/Promise.
   (fn [resolve reject]
     ((fn step []
        (-> (js/Promise.resolve)
            (.then (fn []
                     (when (> rc max-recursion)
                       (throw (js/Error. (str "Recurse count > " max-recursion))))))
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
                                                 false))))))
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
                                      false
                                      (-> (js/Promise.resolve (fun+))
                                          (.then (fn []
                                                   (js/Promise.all [(write+ world [:c i] true) (write+ world [:b i] true)])))
                                          (.then (fn [] true))))))))))
            (.then (fn [done?]
                     (if done?
                       (resolve)
                       (js/setTimeout step 1))))
            (.catch (fn [e]
                      (reject e)))))))))

(defn process+ [{:keys [!critical]} id]
  (-> (js/Promise.resolve)
      (.then (fn []
               (when @!critical
                 (throw (js/Error. "Another process is already in critical section")))
               (prn [::enter id])
               (reset! !critical true)))
      (.then (fn []
               (js/Promise. (fn [resolve]
                              (let [wait-ms (rand-int 20)]
                                (prn [::wait wait-ms])
                                (js/setTimeout resolve wait-ms))))))
      (.then (fn []
               (prn [::leave id])
               (reset! !critical false)))))

(defn ^:export main []
  (println "*** start")
  (let [n 10
        world (make-world n)]
    (-> (->> (range n)
             (map (fn [i] (critical+ world #(process+ world i) i 0)))
             js/Promise.all)
        (.then (fn []
                 (prn [::done]))))))
