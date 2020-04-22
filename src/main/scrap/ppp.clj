(ns scrap.ppp
  (:require [cljs.reader :as r]))

(defn load-env
  {:shadow.build/stage :compile-prepare}
  [arg & more]
  ;; Do nothing. This hook is only used to make sure
  ;; the namespace is loaded
  arg)

(defn ppp [form]
  (let [result-sym (gensym "result")]
    `(let [~result-sym ~form]
       (println
        (str (pr-str '~form) " => "
             (pr-str ~result-sym))))))
