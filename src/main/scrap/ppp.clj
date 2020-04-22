(ns scrap.ppp
  (:require [cljs.reader :as r]))

(defn load-env
  {:shadow.build/stage :compile-prepare}
  [arg & more]
  (prn [::init])
  arg)


(def result-sym (gensym "result"))

(defn ppp [form]
  (let [orig-form form]
    `(let [~result-sym ~form]
       (println
        (str (pr-str '~orig-form) " => "
             (pr-str ~result-sym))))))
