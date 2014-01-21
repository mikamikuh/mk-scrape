(ns mk-scrape.copy)

(defn get-file [target]
  "フォルダ配下のすべてのファイルオブジェクトのシーケンスを取得する"
  (loop [seq (rest (file-seq target)) result []]
    (if (empty? seq)
      result
      (let [res (first seq)]
        (if (.isFile res)
          (recur (rest seq) (conj result res))
          (recur (concat (rest seq) (rest (file-seq res))) result))))))