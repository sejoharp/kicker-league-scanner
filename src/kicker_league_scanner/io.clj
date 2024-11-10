(ns kicker-league-scanner.io
  (:require [clj-http.client :as client]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [hickory.core :as h]
            [kicker-league-scanner.csv-format :as csv-format]
            [kicker-league-scanner.parser :as parser])
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

(defn match->csv-file! [file-writer match]
  (csv/write-csv file-writer (csv-format/match->csv match) :separator \;))

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

(defn get-season! [season]
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

(defn upload-file! [domain user password content-as-inputstream]
  (client/put (str "https://" domain "/remote.php/dav/files/" user "-games/all-games.csv.bz2")
              {:body          content-as-inputstream
               :basic-auth    [user password]
               :cookie-policy :standard}))

(defn delete-old-file! [domain user password]
  (client/delete (str "https://" domain "/remote.php/dav/files/" user "/all-games/all-games.csv.bz2")
                 {:basic-auth    [user password]
                  :cookie-policy :standard}))

(defn upload-matches! [domain user password matches]
  (let [matches-as-byte-array (create-matches-as-byte-array matches)]
    (delete-old-file! domain user password)
    (upload-file! domain user password (io/input-stream matches-as-byte-array))))

(defn upload-all-matches-to-nextcloud! [{:keys [target-domain target-user target-password match-directory-path] :as options}]
  (log/info "uploading all matches to nextcloud ..")
  (log/info "options: " (assoc options :target-password "***"))
  (->> match-directory-path
       read-match-files
       (map read-match-from-edn)
       (upload-matches! target-domain target-user target-password)))

(defn log-parsing-link [link]
  (prn (str "parsing " link))
  link)

(def parse-match-from-link-fn (comp
                               parser/parse-valid-match
                               html->hickory
                               log-parsing-link))

(defn match->status-entry [match]
  {:date       (:date match)
   :home-team  (:home-team match)
   :guest-team (:guest-team match)
   :link       (:link match)})

(defn load-season! [{:keys [season match-directory-path]
                     :as   options}]
  (log/info "downloading matches ..")
  (log/info "options: " options)
  (let [found-matches (->> season
                           get-season!
                           parser/get-league-links-from-league-overview
                           (map html->hickory)
                           (map parser/get-match-links-from-league)
                           flatten)
        new-matches (filter (partial new-match? match-directory-path) found-matches)
        parsed-matches (map parse-match-from-link-fn new-matches)
        valid-matches (filter some? parsed-matches)
        new-state {:found-match-count  (count found-matches)
                   :new-match-count    (count new-matches)
                   :parsed-match-count (count parsed-matches)
                   :valid-match-count  (count valid-matches)
                   :valid-matches      (map match->status-entry valid-matches)
                   :last-run           (parser/current-user-friendly-timestamp)}]
    (log/info "new state: " new-state)
    (matches->edn-files! match-directory-path valid-matches)
    new-state))

(defn update-data! [options app-status]
  (let [new-state (load-season! options)]
    (when (> (:new-match-count new-state) 0)
      (upload-all-matches-to-nextcloud! options)
      (reset! app-status new-state))))

(comment
  (load-season! {:match-directory-path cli/default-downloaded-matches-directory
                 :season               io/current-season})
  (save-all-matches-to-csv-file {:match-directory-path cli/default-downloaded-matches-directory
                                 :target-csv-file      cli/default-csv-file-path}))