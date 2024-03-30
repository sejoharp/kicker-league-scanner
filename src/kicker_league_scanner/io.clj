(ns kicker-league-scanner.io
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def default-downloaded-matches-directory "downloaded-matches")
(def default-csv-file-path "./all-games.csv")
(def league-overview-season-link "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen")

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