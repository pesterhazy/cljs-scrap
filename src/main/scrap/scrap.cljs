(ns scrap.scrap
  (:require ["@sinonjs/fake-timers" :as fake-timers]
            [clojure.test :as t]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; promise helpers

(defn delay+ [wait-ms]
  (js/Promise. (fn [resolve]
                 (js/setTimeout resolve wait-ms))))

(defn make-ticker [clock]
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
                 (let [[p break-fn] (make-ticker clock)]
                   (js/Promise.all [(-> (fun+) (.then break-fn)) p]))))
        (.finally (fn []
                    (prn [::uninstalling])
                    (.uninstall clock))))))

(defn time+ [fun+]
  (let [start (js/performance.now)]
    (-> (js/Promise.resolve)
        (.then fun+)
        (.finally (fn []
                    (prn [::elapsed (- (js/performance.now) start)]))))))

(defn promise-test [^js/Promise p]
  (reify
    clojure.test/IAsyncTest
    clojure.core/IFn
    (-invoke [_ done]
      (-> p
          (.catch (fn [e]
                    (js/console.error e)
                    (t/is false (str "Promise rejected: " (.-message e)))))
          (.finally done)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; simulation

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
      (.then (fn []
               (get-in @!state path)))))

(defn process+ [{:keys [!critical !counter]} id]
  (-> (js/Promise.resolve)
      (.then (fn []
               (when @!critical
                 (throw (js/Error. "Another process is already in critical section")))
               #_(prn [::enter id])
               (reset! !critical true)))
      (.then (fn []
               (delay+ (rand-int 10))))
      (.then (fn []
               #_(prn [::leave id])
               (swap! !counter inc)
               (reset! !critical false)))))

(defn simulation+ [world n n-processes mutex+]
  (->> (range n)
       (map (fn [i] (nth (iterate (fn [p] (.then p (fn [] (mutex+ world #(process+ world i) i)))) (js/Promise.resolve))
                         n-processes)))
       js/Promise.all))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; main code

(def max-recursion 1000)

(defn dijkstra+ [{:keys [n] :as world} fun+ i]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; test code

(t/deftest multiple
  (let [n 5
        world (make-world n)
        times 5]
    (-> (js/Promise.resolve)
        (.then (fn []
                 (time+ (fn [] (with-fake-clock+ (fn [] (simulation+ world n times dijkstra+)))))))
        (.then (fn []
                 (t/is (= (* n times) (-> world :!counter deref)))))
        promise-test)))

(defn ^:export main []
  (t/run-tests))
