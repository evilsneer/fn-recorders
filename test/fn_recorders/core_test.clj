(ns fn-recorders.core-test
  (:require [clojure.test :refer :all]
            [fn-recorders.core :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :as cheshire]))

(def csv-file-name "test-times.csv")
(def json-file-name "test-results.json")

(defn clean-atoms [f]
  (add-file-watch-to-recorded! :times :->csv csv-file-name)
  (add-file-watch-to-recorded! :results :->json json-file-name)
  (reset! recorded-times [])
  (reset! recorded-results [])
  (f)
  (remove-watch recorded-results :->json)
  (remove-watch recorded-times :->csv)
  (reset! recorded-times [])
  (reset! recorded-results [])
  (io/delete-file csv-file-name)
  (io/delete-file json-file-name))

(use-fixtures :once clean-atoms)

(defn long-f []
  (let [wait-for (rand-int 300)]
    (Thread/sleep wait-for)
    (str wait-for "!")))

(deftest record-times-for-test
  (testing "recorded times + csv"
    (let [recorded-long-f #(record-times-for :long-f (long-f))]
      (is (= 0 (count @recorded-times)))
      (->> recorded-long-f
        (repeatedly 5)
        doall)
      (is (= 5 (count @recorded-times)))
      (is (.exists (io/file csv-file-name)))
      (is (= 5 (-> csv-file-name slurp str/split-lines count)))))

  (testing "recorded results + json"
    (let [recorded-long-f #(record-results-for {:long-f "meme"} (long-f))]
      (is (= 0 (count @recorded-results)))
      (binding [*middleware* [#(str/replace % #"!" "") read-string]]
        (->> recorded-long-f
         (repeatedly 5)
         doall))
      (is (= 5 (count @recorded-results)))
      (is (.exists (io/file json-file-name)))
      (is (number? (-> json-file-name
                     slurp
                     (cheshire/parse-string true)
                     first
                     :result))))))
