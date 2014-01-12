(ns mk-scrape.converter
  (:require
   [clojure.string :as str]
   [net.cgrand.enlive-html :as html]))

(defn scrape [resource condition]
  "リソースと条件から要素を取得する"
  (map html/text (html/select resource condition)))

(defn fetch-page [path]
  "pathのHTMLをシーケンスに変換したものを返す"
  (do
    (println path)
    (if (.endsWith path ".html")
      (html/html-resource
        (java.io.StringReader. (slurp path)))
      nil)))

(defn create-paths [folder]
  "パスの一覧を作成する"
  (rest (map #(.toString %) (file-seq (clojure.java.io/file folder)))))

(defn fetch [paths]
  "パスのシーケンスを受け取り、その全要素に対してfetch-pageを実施したものを返す"
  (map #(fetch-page %) paths))

(defn convert-to-map [targets]
  "解析したHTMLのシーケンスを取得し、出力用マップのシーケンスを返す"
  (map #(merge {:content (scrape % [:span.made_prev_txt])}
                 {:title (let [res (scrape % [:div.topics_path :ul :li])] (nth res (dec (count res))))}
                 {:img (let [seq (:src (:attrs (first (html/select % [:div.pic :p :img]))))]
                         (str "/img/items/"
                              (let [s (clojure.string/split seq #"/")]
                                (nth s (dec (count s))))))}
                 {:layout "post"}
                 {:author (first (reverse (str/split (:href (:attrs (first (html/select % [:div.inner :dt :a]))))  #"\/")))}) (remove nil? targets)))

(defn convert-to-seq [m]
  "マップを文字列シーケンスに変換する"
  ["---"
     (str "title: " (clojure.string/replace (m :title) #"\n" ""))
     (str "author: " (m :author))
     (str "layout: post")
     (str "img: " (m :img))
     "---"
     (first (m :content))
     ])

(defn convert-to-string [seq]
  "文字列のシーケンスを、改行区切りのシーケンスに変換する"
  (loop [s seq result ""] (if (empty? s) result (recur (rest s) (str result (first s) "\n")))))

(defn execute [folder]
  "フォルダ配下のファイルをスクレイピングする"
  (let [paths (create-paths folder)]
           (map convert-to-string
                (map 
                  convert-to-seq (convert-to-map (fetch paths))))))