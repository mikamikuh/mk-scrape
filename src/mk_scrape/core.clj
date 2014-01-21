(ns mk-scrape.core
  (:require
   [mk-scrape.converter :as conv]
   [mk-scrape.copy :as copy]
   [mk-scrape.export :as export]))

(defn convert-to-map [folder]
  "フォルダ配下のファイルを読み取り、マップに変換"
  (let [paths (conv/create-paths folder)]
    (conv/convert-to-map (conv/fetch paths))))

(defn get-img-list [folder]
  "マップのシーケンスから:imgの値をすべて取得する"
  (map #(let [seq (clojure.string/split (% :img) #"/")] (nth seq (dec (count seq))))
       (convert-to-map folder)))

(defn get-copy-resource [folder]
  "コピー対象のリソースをすべて取得する"
  (let [files (copy/get-file (clojure.java.io/file folder)) list (get-img-list folder)]
    (map (fn [name] (first (filter #(= (.getName %) name) files))) list)))

(defn copy-resources [destination source]
  "リソースをコピーする"
  (loop [seq (get-copy-resource source)]
    (if (empty? seq)
      nil
      (let [file (first seq)]
        (do (clojure.java.io/copy file
                              (clojure.java.io/file (str destination
                                                         (.getName file))))
          (recur (rest seq)))))))

(defn export-md [destination source]
  "mdファイルを生成する"
  (loop [seq (conv/execute source)]
    (if (empty? seq)
      nil
      (do
        (export/export (first seq) destination)
        (recur (rest seq))))))

(defn export-md2 [destination source]
  "mdファイルを生成する"
  (export/export (conv/execute source) destination))