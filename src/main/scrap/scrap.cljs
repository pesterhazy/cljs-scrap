(ns scrap.scrap
  (:require ["@sinonjs/fake-timers" :as fake-timers]))

(defn make-world [n]
  (let [state {:k 0
               :b (vec (repeat n true))
               :c (vec (repeat n true))}]
    {:n n
     :!critical (atom nil)
     :!counter (atom 0)
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

(def max-recursion 1000)

(defn critical+ [{:keys [n] :as world} fun+ i]
  (js/Promise.
   (fn [resolve reject]
     ((fn step [rc]
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
                       (js/setTimeout (fn [] (step (inc rc))) 1))))
            (.catch (fn [e]
                      (reject e)))))
      0))))

(defn advance+ [clock]
  (let [!continue (atom true)
        break-fn (fn [] (reset! !continue false))]
    [(js/Promise. (fn [resolve]
                    ((fn step [n]
                       (if (and @!continue(< n 10000))
                         (do
                           (.tick clock 1)
                           (.then (js/Promise.resolve) #(step (inc n))))
                         (resolve)))
                     0)))
     break-fn]))

(defn with-fake-clock+ [fun+]
  (let [clock (fake-timers/install)]
    (prn [::installed])
    (-> (js/Promise.resolve)
        (.then (fn []
                 (let [[p break-fn] (advance+ clock)]
                   (js/Promise.all [(-> (fun+) (.then break-fn)) p]))))
        (.finally (fn []
                    (prn [::uninstalling])
                    (.uninstall clock)
                    (prn [::ok]))))))

(defn time+ [fun+]
  (let [start (js/performance.now)]
    (-> (js/Promise.resolve)
        (.then fun+)
        (.finally (fn []
                    (prn [::elapsed (- (js/performance.now) start)]))))))

(defn process+ [{:keys [!critical !counter]} id]
  (-> (js/Promise.resolve)
      (.then (fn []
               (when @!critical
                 (throw (js/Error. "Another process is already in critical section")))
               (prn [::enter id])
               (reset! !critical true)))
      (.then (fn []
               (delay+ (rand-int 10))))
      (.then (fn []
               (prn [::leave id])
               (swap! !counter inc)
               (reset! !critical false)))))

(defn simulation+ [world n n-processes]
  (->> (range n)
       (map (fn [i] (nth (iterate (fn [p] (.then p (fn [] (critical+ world #(process+ world i) i)))) (js/Promise.resolve))
                         n-processes)))
       js/Promise.all))

(defn ^:export main []
  (println "*** start")
  (let [n 10
        world (make-world n)
        times 10]
    (-> (js/Promise.resolve)
        (.then (fn []
                 (time+ (fn [] (with-fake-clock+ (fn [] (simulation+ world n times)))))))
        (.then (fn []
                 (assert (= (* n times) (-> world :!counter deref)) (str "Unexpected: " (-> world :!counter deref)))
                 (println "*** end"))))))
