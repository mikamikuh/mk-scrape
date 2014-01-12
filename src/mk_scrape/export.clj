(ns mk-scrape.export
  (:require
    [clojure.java.io :as io]
    [clj-time.core :as time]
    [clj-time.format :as format]))

(def custom-formatter (format/formatter "yyyy-MM-dd"))
(def initial-date (time/date-time 2000 1 1))

(defn export [data base-path]
  "データをbase-pathにmdファイルとして出力する"
  (loop [seq data date initial-date n 1]
    (if (empty? seq)
      nil
      (do 
           (with-open [wrtr (io/writer (str base-path (format/unparse custom-formatter date) "-" n ".md"))] (.write wrtr (first seq)))
           (recur (rest seq) (time/plus date (time/days 1)) (+ n 1))))))