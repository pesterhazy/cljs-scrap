(ns scrap.scrap
  (:require [clojure.test :as t]
            [scrap.dijkstra]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^:export main []
  (t/run-tests 'scrap.dijkstra))
