(ns kicker-league-scanner.io
  (:require [clj-http.client :as client]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hickory.core :as h])
  (:import (java.io ByteArrayOutputStream)
           (org.apache.commons.compress.compressors.bzip2 BZip2CompressorInputStream BZip2CompressorOutputStream)))

(def league-overview-season-link "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen")

(def season-year->id {"2024/25" "26"
                      "2023/24" "24"
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
(def current-season "2024/25")

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
                                             guest-points)]))))))

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
      (>= month 10) (str year "/04"))))

(defn game->csv [match game]
  (let [home-players (:names (:home game))
        guest-players (:names (:guest game))
        game-points (calculate-game-points game)
        quarter (calculate-quarter (:date match))]
    [[(:date match)
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
      1]
     [(:date match)
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
      1]
     [(:date match)
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
      1]
     [(:date match)
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
      1]]))

(defn match->csv [{games :games :as match}]
  (let [game->csv-fn (partial game->csv match)]
    (reduce (fn [acc game]
              (into acc (game->csv-fn game)))
            []
            games)))

(defn match->csv-file! [file-writer match]
  (csv/write-csv file-writer (match->csv match) :separator \;))

(defn matches->csv-file! [file-path matches]
  (io/make-parents file-path)
  (with-open [file-writer (io/writer (BZip2CompressorOutputStream. (io/output-stream file-path)))]
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

(defn read-bzip2-as-string [file-path]
  (with-open [file-stream (io/input-stream file-path)
              bzip2-stream (BZip2CompressorInputStream. file-stream)
              reader (io/reader bzip2-stream)]
    (doall
     (csv/read-csv reader {:separator \;}))))

#_(defn read-bzip2-csv [file-path]
    (with-open [file-stream (io/input-stream file-path)
                bzip2-stream (BZip2CompressorInputStream. file-stream)
                reader (io/reader bzip2-stream)]
      (doall
       (csv/read-csv reader))))

(defn delete-file [path]
  (io/delete-file path true))

(defn read-directory [directory] (clojure.java.io/file directory))

(defn read-match-files [directory] (rest (file-seq (read-directory directory))))

(defn save-all-matches-to-csv-file [{:keys [target-csv-file match-directory-path] :as options}]
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

(defn create-matches-as-byte-array [matches]
  (let [output-stream (ByteArrayOutputStream.)
        bzip2-output-stream (BZip2CompressorOutputStream. output-stream)]
    (with-open [writer (io/writer bzip2-output-stream)]
      (doseq [match matches]
        (match->csv-file! writer match)))
    (.toByteArray ^ByteArrayOutputStream output-stream)))

(defn upload-file [domain user password filename content-as-inputstream]
  (client/put (str "https://" domain "/remote.php/dav/files/" user "/all-games/" filename)
              {:body          content-as-inputstream
               :basic-auth    [user password]
               :cookie-policy :standard}))

(defn upload-matches [domain user password filename matches]
  (let [matches-as-byte-array (create-matches-as-byte-array matches)]
    (upload-file domain user password filename (io/input-stream matches-as-byte-array))))

(defn save-all-matches-to-nextcloud [{:keys [target-domain target-user target-password target-file-name match-directory-path] :as options}]
  (prn "uploading all matches to nextcloud ..")
  (prn "options: " (assoc options :target-password "***"))
  (->> match-directory-path
       read-match-files
       (map read-match-from-edn)
       (upload-matches target-domain target-user target-password target-file-name)))