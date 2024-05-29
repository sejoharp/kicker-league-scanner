(ns kicker-league-scanner.io
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hickory.core :as h]
            [java-time.api :as jt]
            [lein-project-reader.core :as lpr]))

(def default-downloaded-matches-directory "downloaded-matches")
(def now-in-readable-format
  (jt/format "YYYY-MM-dd" (jt/local-date-time)))
(def default-csv-file-path
  (str "./all-games-" now-in-readable-format ".csv"))
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

(defn calculate-game-points [game] (cond
                                     (= 6 (:score (:home game))) [2 0]
                                     (= 6 (:score (:guest game))) [0 2]
                                     (= (:score (:home game)) (:score (:guest game))) [1 1]
                                     :else [0 0]))

(defn calculate-match-score [games]
  (let [game-points (map calculate-game-points games)]
    (loop [game-points-list game-points
           result [0 0]]
      (if (= 0 (count game-points-list))
        result
        (let [home-points (first (first game-points-list))
              guest-points (second (first game-points-list))]
          (recur (rest game-points-list) [(+ (first result)
                                             home-points)
                                          (+ (second result)
                                             guest-points)])))))
  )

(defn calculate-points [match]
  (let [match-scores (calculate-match-score (:games match))]
    (cond
      (= (first match-scores) (second match-scores)) [1 1]
      (> (first match-scores) (second match-scores)) [2 0]
      (< (first match-scores) (second match-scores)) [0 2])))

(defn calculate-quarter [date]
  (let [month (Integer/parseInt (second (str/split date #"-")))
        year (first (str/split date #"-"))]
    (cond
      (<= month 3) (str year "/01")
      (<= month 6) (str year "/02")
      (<= month 9) (str year "/03")
      (>= month 10) (str year "/04")
      )))

(defn game->csv [match game]
  (let [home-players (:names (:home game))
        guest-players (:names (:guest game))
        game-points (calculate-game-points game)
        quarter (calculate-quarter (:date match))]
    [(str/join ";" [(:date match)
                    (:match-day match)
                    (:position game)
                    "H"
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
                    (:guest-team match)
                    "G"
                    (first game-points)
                    (second game-points)
                    quarter
                    "1"])
     (str/join ";" [(:date match)
                    (:match-day match)
                    (:position game)
                    "H"
                    (:home-team match)
                    (if (= 2 (count home-players))
                      (second home-players)
                      "XXXX")
                    (first home-players)
                    (:score (:home game))
                    (:score (:guest game))
                    (if (= 2 (count guest-players))
                      (second guest-players)
                      "XXXX")
                    (first guest-players)
                    (:guest-team match)
                    "G"
                    (first game-points)
                    (second game-points)
                    quarter
                    "1"])
     (str/join ";" [(:date match)
                    (:match-day match)
                    (:position game)
                    "G"
                    (:guest-team match)
                    (first guest-players)
                    (if (= 2 (count guest-players))
                      (second guest-players)
                      "XXXX")
                    (:score (:guest game))
                    (:score (:home game))
                    (first home-players)
                    (if (= 2 (count home-players))
                      (second home-players)
                      "XXXX")
                    (:home-team match)
                    "H"
                    (second game-points)
                    (first game-points)
                    quarter
                    "1"])
     (str/join ";" [(:date match)
                    (:match-day match)
                    (:position game)
                    "G"
                    (:guest-team match)
                    (if (= 2 (count guest-players))
                      (second guest-players)
                      "XXXX")
                    (first guest-players)
                    (:score (:guest game))
                    (:score (:home game))
                    (if (= 2 (count home-players))
                      (second home-players)
                      "XXXX")
                    (first home-players)
                    (:home-team match)
                    "H"
                    (second game-points)
                    (first game-points)
                    quarter
                    "1"])]))

(defn match->csv [{games :games :as match}]
  (let [game->csv-fn (partial game->csv match)]
    (flatten (map game->csv-fn games))))

(defn match->csv-file! [file-writer match]
  (doseq [game-string (match->csv match)]
    (.write file-writer (str game-string "\n"))))

(defn matches->csv-file! [file-path matches]
  (io/make-parents file-path)
  (with-open [file-writer (clojure.java.io/writer file-path)]
    (doseq [match matches]
      (match->csv-file! file-writer match))))

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

(defn save-all-matches-to-csv [{:keys [target-csv-file match-directory-path]
                                :as   options}]
  (prn "exporting matches to csv ..")
  (prn "options: " options)
  (->> match-directory-path
       read-match-files
       (map read-match-from-edn)
       (matches->csv-file! target-csv-file)))

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

(defn create-cli-config [load-season-fn]
  {:app         {:command     "kicker-league-scanner"
                 :description "A command-line kicker stats scanner"
                 :version     (:version (lpr/read-project))}
   :global-opts [{:option  "match-directory-path"
                  :short   "mdp"
                  :as      (str "Location of all matches.")
                  :type    :string
                  :default default-downloaded-matches-directory}]
   :commands    [{:command     "download" :short "d"
                  :description ["downloads all matches for the given season"]
                  :opts        [{:option  "season"
                                 :short   "s"
                                 :as      "target season"
                                 :type    :string
                                 :default current-season}]
                  :runs        load-season-fn}
                 {:command     "export" :short "s"
                  :description "exports all matches to a given csv file"
                  :opts        [{:option  "target-csv-file"
                                 :short   "tcf"
                                 :as      (str "Location for the csv file with all games.")
                                 :type    :string
                                 :default default-csv-file-path}]
                  :runs        save-all-matches-to-csv}]})
