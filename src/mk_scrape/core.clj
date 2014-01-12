(ns mk-scrape.core
  (:require
   [mk-scrape.converter :as conv]
   [mk-scrape.copy :as copy]
   [mk-scrape.export :as export]))

(defn convert-to-map [folder]
  "フォルダ配下のファイルを読み取り、マップに変換する"
  (let [paths (conv/create-paths folder)]
    (conv/convert-to-map (conv/fetch paths))))

(defn get-img-list [folder]
  "マップのシーケンスから:imgの値をすべて取得する"
  (map #(let [seq (clojure.string/split (% :img) #"/")] (nth seq (dec (count seq))))
       (convert-to-map folder)))

(defn get-copy-resource [folder]
  "コピー対象のリソースをすべて取得する"
  (map #(copy/get-file (clojure.java.io/file folder) %) (get-img-list folder)))