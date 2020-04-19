(ns scrap.dijkstra
  (:require ["@sinonjs/fake-timers" :as fake-timers]
            [clojure.test :as t]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; promise helpers

(defn pnow []
  (if (exists? js/performance)
    (js/performance.now)
    (.now (.-performance (js/require "perf_hooks")))))

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
    #_(prn [::installed])
    (-> (js/Promise.resolve)
        (.then (fn []
                 (let [[p break-fn] (make-ticker clock)]
                   (js/Promise.all [(-> (fun+) (.then break-fn)) p]))))
        (.finally (fn []
                    #_(prn [::uninstalling])
                    (.uninstall clock))))))

(defn time+ [fun+]
  (let [start (pnow)]
    (-> (js/Promise.resolve)
        (.then fun+)
        (.finally (fn []
                    (prn [::elapsed (- (pnow) start)]))))))

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
;; store

(defprotocol Store
  (write+ [_ path v])
  (read+ [_ path]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; simulation

(defrecord SimulationStore
    [n !critical !counter !state]
  Store
  (write+ [_ path v]
    (-> (js/Promise.resolve)
        (.then (fn []
                 (swap! !state assoc-in path v)))))
  (read+ [_ path]
    (-> (js/Promise.resolve)
        (.then (fn []
                 (get-in @!state path))))))

(defn make-simulation-store [n]
  (let [state {:k 0
               :b (vec (repeat n true))
               :c (vec (repeat n true))}]
    (map->SimulationStore
     {:n n
      :!critical (atom nil)
      :!counter (atom 0)
      :!state (atom state)})))

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

(defn simulation+ [store n n-processes mutex+]
  (->> (range n)
       (map (fn [i] (nth (iterate (fn [p] (.then p (fn [] (mutex+ store #(process+ store i) i n)))) (js/Promise.resolve))
                         n-processes)))
       js/Promise.all))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; main code

(defn dijkstra+
  "Implementation of the algorithm described in E.W. Dijkstra, \"Solution
  of a problem in concurrent programming control\" (1965)"
  [store fun+ i n]
  (js/Promise.
   (fn [resolve reject]
     ((fn step []
        (-> (js/Promise.resolve)
            (.then (fn []
                     (write+ store [:b i] false)))
            (.then (fn []
                     (read+ store [:k])))
            (.then (fn [k]
                     (if (not= k i)
                       (-> (write+ store [:c i] true)
                           (.then (fn []
                                    (read+ store [:b k])))
                           (.then (fn [b-k]
                                    (-> (js/Promise.resolve
                                         (when b-k (write+ store [:k] i)))
                                        (.then (fn []
                                                 false))))))
                       (-> (write+ store [:c i] false)
                           (.then (fn []
                                    (-> (->> (range n)
                                             (map (fn [j]
                                                    (if (= j i)
                                                      true
                                                      (read+ store [:c j]))))
                                             (js/Promise.all))
                                        (.then (fn [bools]
                                                 (every? identity bools))))))
                           (.then (fn [ok?]
                                    (if-not ok?
                                      false
                                      (-> (js/Promise.resolve (fun+))
                                          (.then (fn []
                                                   (js/Promise.all [(write+ store [:c i] true) (write+ store [:b i] true)])))
                                          (.then (fn [] true))))))))))
            (.then (fn [done?]
                     (if done?
                       (resolve)
                       (js/setTimeout (fn [] (step))))))
            (.catch (fn [e]
                      (reject e)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; test code

(t/deftest multiple
  (let [n 5
        store (make-simulation-store n)
        times 5]
    (-> (js/Promise.resolve)
        (.then (fn []
                 (time+ (fn [] (with-fake-clock+ (fn [] (simulation+ store n times dijkstra+)))))))
        (.then (fn []
                 (t/is (= (* n times) (-> store :!counter deref)))))
        promise-test)))
