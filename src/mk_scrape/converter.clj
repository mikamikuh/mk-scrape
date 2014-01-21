(ns mk-scrape.converter
  (:require
   [clojure.string :as str]
   [net.cgrand.enlive-html :as html]))

(def ignore-files ["share_tools.html" "user_tools.html" "email_sharing.html" "filing_tools.html" "global_tools.html"])

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
  (filter #(and (empty? (filter (fn [f] (.endsWith % f)) ignore-files)) (.endsWith % ".html"))
          (map #(.toString %) (file-seq (clojure.java.io/file folder)))))

(defn fetch [paths]
  "パスのシーケンスを受け取り、その全要素に対してfetch-pageを実施したものを返す"
  (map #(fetch-page %) paths))

(defn convert-to-map [targets]
  "解析したHTMLのシーケンスを取得し、出力用マップのシーケンスを返す"
  (map #(merge {:content (scrape % [:span.made_prev_txt])}
                 {:title (let [res (scrape % [:div.topics_path :ul :li])]
                           (if (empty? res)
                             ""
                             (nth res (dec (count res)))))}
                 {:img (let [seq (:src (:attrs (first (html/select % [:div.pic :p :img]))))]
                         (str ""
                              (if (nil? seq)
                                ""
                                (let [s (clojure.string/split seq #"/")]
                                  (if (empty? s)
                                    ""
                                    (nth s (dec (count s))))))))}
                 {:link (:href (:attrs (first (html/select % [:span.goodCount :a])))) }
                 {:layout "post"}
                 {:author (let [seq (:href (:attrs (first (html/select % [:dl.authorDetail01 :dt :a]))))]
                            (if (nil? seq)
                              ""
                              (first (reverse (str/split seq #"\/")))))})
       (remove nil? targets)))

(defn convert-to-seq [m]
  "マップを文字列シーケンスに変換する"
  ["---"
     (str "title: " (clojure.string/replace (m :title) #"\n" ""))
     (str "author: " (m :author))
     (str "layout: post")
     (str "img: " (m :img))
     (str "link: " (m :link))
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