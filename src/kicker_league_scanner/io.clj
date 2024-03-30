(ns kicker-league-scanner.io
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hickory.core :as h]))

(def default-downloaded-matches-directory "downloaded-matches")
(def default-csv-file-path "./all-games.csv")
(def league-overview-season-link "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen")
(def season-year->id {"2023/24" "24"
                      "2022/23" "23"
                      "2022"    "22"
                      "2019/20" "20"
                      "2018/19" "16"
                      "2018"    "13"
                      "2017"    "12"
                      "2016"    "11"
                      "2015"    "9"
                      "2014"    "8"
                      "2013"    "7"
                      "2012"    "4"
                      "2011"    "3"
                      "2010"    "2"
                      "2009"    "1"})
(def current-season "2023/24")

(defn game->csv [match game]
  (let [home-players (:names (:home game))
        guest-players (:names (:guest game))]
    (str/join ";" [(:date match)
                   (:match-day match)
                   (:position game)
                   (:home-team match)
                   (first home-players)
                   (if (= 2 (count home-players))
                     (second home-players)
                     "XXXX")
                   (:score (:home game))
                   (:score (:guest game))
                   (first guest-players)
                   (if (= 2 (count guest-players))
                     (second guest-players)
                     "XXXX")
                   (:guest-team match)])))

(defn match->csv [{games :games :as match}]
  (let [game->csv-fn (partial game->csv match)]
    (map game->csv-fn games)))

(defn match->csv-file! [file-path match]
  (io/make-parents file-path)
  (doseq [game-string (match->csv match)]
    (spit file-path
          (str game-string "\n")
          :append true)))

(defn matches->csv-file! [file-path matches]
  (doseq [match matches]
    (match->csv-file! file-path match)))

(defn link->filename [link]
  (-> link
      (#(str/split % #"\?"))
      second
      (#(str/replace % #"&" "-"))
      (#(str % ".edn"))))

(defn match->edn-file! [path match]
  (let [filename (->> match
                      :link
                      link->filename)
        path (str path "/" filename)]
    (io/make-parents path)
    (spit path
          (clojure.core/pr-str match))))

(defn matches->edn-files! [match-directory-path matches]
  (doseq [match matches]
    (match->edn-file! match-directory-path match)))

(defn new-match? [directory link]
  (let [filename (link->filename link)]
    (not (.exists
           (io/file (str directory "/" filename))))))

(defn read-match-from-edn [file-path]
  (->> file-path
       slurp
       read-string))

(defn read-match-from-csv [file-path]
  (->> file-path
       slurp))

(defn delete-file [path]
  (io/delete-file path true))

(defn read-directory [directory] (clojure.java.io/file directory))

(defn read-match-files [directory] (rest (file-seq (read-directory directory))))

(defn save-all-matches-to-csv [{:keys [csv-file-path match-directory-path]
                                :as   options}]
  (prn "exporting matches to csv ..")
  (prn "options: " options)
  (->> match-directory-path
       read-match-files
       (map read-match-from-edn)
       (matches->csv-file! csv-file-path)))

(defn html->hickory [overview-link]
  (let [html (slurp overview-link)]
    (h/as-hickory (h/parse html))))


(defn get-season [season]
  (->> {:form-params {:filter_saison_id (get season-year->id season)
                      :ok               "Los"
                      :task             "veranstaltungen"}}
       (client/post league-overview-season-link)
       :body
       h/parse
       h/as-hickory))
