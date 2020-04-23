(ns scrap.ppp
  (:require [cljs.reader :as r]))

(defn load-env
  {:shadow.build/stage :compile-prepare}
  [arg & more]
  ;; Do nothing. This hook is only used to make sure
  ;; the namespace is loaded
  arg)

(defn hashpr [form]
  (let [result-sym (gensym "result")]
    `(let [~result-sym ~form]
       (js/console.warn
        (binding [clojure.core/*print-length* 20]
          (str (pr-str '~form) " => "
               (pr-str ~result-sym))))
       ~result-sym)))

(defn hashpr! [form]
  (let [result-sym (gensym "result")]
    `(let [~result-sym ~form]
       (js/console.warn
        (str (pr-str '~form) " => "
             (pr-str ~result-sym)))
       ~result-sym)))

(defn hashpp [form]
  (let [result-sym (gensym "result")]
    `(let [~result-sym ~form]
       (js/console.warn
        (binding [clojure.core/*print-length* 20]
          (str (with-out-str (clojure.pprint/pprint '~form)) " => "
               (with-out-str (clojure.pprint/pprint ~result-sym)))))
       ~result-sym)))

(defn hashpp! [form]
  (let [result-sym (gensym "result")]
    `(let [~result-sym ~form]
       (js/console.warn
        (str (with-out-str (clojure.pprint/pprint '~form)) " => "
             (with-out-str (clojure.pprint/pprint ~result-sym))))
       ~result-sym)))

(defn hashpc [form]
  (let [result-sym (gensym "result")]
    `(let [~result-sym ~form]
       (js/console.warn
        '~form " => "
        ~result-sym)
       ~result-sym)))
