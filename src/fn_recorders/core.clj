(ns fn-recorders.core
  (:require [cheshire.core :as cheshire]
            [clojure.string :as str]))

(defn ->json [name data]
  (with-open [w (clojure.java.io/writer name)]
    (-> data
      (cheshire/generate-stream w))))

(defn ->csv [name data]
  (->> data
    (map (comp (partial str/join "\t") vals))
    (str/join "\n")
    (spit name)))


;; left to ringt
(def ^:dynamic *middleware* [identity])

(defonce recorded-times (atom []))

(defonce recorded-results (atom []))

(defn add-file-watch-to-recorded! [to type filename]
  {:pre [(#{:->json :->csv} type)
         (#{:times :results} to)]}
  (let [f (case type
            :->json ->json
            :->csv ->csv)
        a (case to
            :times recorded-times
            :results recorded-results)]
    (add-watch a type #(f filename %4))))

(defrecord FnRunTime [axis time])
(defrecord FnRunResult [axis result])

(defmacro record-times-for [info & body]
  `(let [start-time# (System/currentTimeMillis)
         result#     (do ~@body)]
     (swap! recorded-times conj
       (->FnRunTime
         ~info
         (- (System/currentTimeMillis) start-time#)))
     result#))

(defmacro record-results-for [info & body]
  `(let [result# (do ~@body)
         middleware-fn# (apply comp (reverse *middleware*))]
     (swap! recorded-results conj
       (->FnRunResult
         ~info
         (-> result# middleware-fn#)))
     result#))