(ns mk-scrape.copy)

(defn get-file [target name]
  "フォルダ配下のすべてのファイルを探索し、対象のファイル名のFileオブジェクトを取得する"
  (loop [seq (rest (file-seq target))]
    (if (empty? seq)
      nil
      (let [res (first seq)]
        (if (.isFile res)
          (if (= (.getName res) name)
            res
            (recur (rest seq)))
          (recur (concat (rest seq) (rest (file-seq res)))))))))