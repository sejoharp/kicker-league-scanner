(ns kicker-league-scanner.core
  (:require [cli-matic.core :as cli]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hickory.core :as h]
            [hickory.select :as s])

  (:gen-class))

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
(def league-overview-season-link "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen")
(def current-season "2023/24")
(def default-downloaded-matches-directory "downloaded-matches")
(def default-csv-file-path "./all-games.csv")

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

(defn add-kickern-hamburg-domain [path]
  (str "https://kickern-hamburg.de" path))

(defn get-league-links-from-league-overview [parsed-html]
  (let [link-snippets (s/select (s/descendant (s/class "readon"))
                                parsed-html)
        league-snippets (filter #(str/includes? (:content %) "Begegnungen …")
                                link-snippets)
        link-paths (map #(get-in % [:attrs :href]) league-snippets)]
    (map add-kickern-hamburg-domain link-paths)))

(defn completed-match? [[result-info & remaining]]
  (let [first-character (first result-info)]
    (contains? (set "0123456789") first-character)))

(defn get-match-links-from-league [parsed-html]
  (let [link-snippets (s/select (s/descendant (s/or (s/class "sectiontableentry1")
                                                    (s/class "sectiontableentry2"))
                                              (s/tag :a))
                                parsed-html)]
    (->> link-snippets
         (filter #(and
                    (completed-match? (:content %))
                    (some? (get-in % [:attrs :href]))
                    (str/includes? (get-in % [:attrs :href])
                                   "begegnung_spielplan")))
         (map #(get-in % [:attrs :href]))
         (map add-kickern-hamburg-domain))))

(defn find-game-snippets [match-page]
  (s/select (s/descendant (s/or (s/class "sectiontableentry1")
                                (s/class "sectiontableentry2")))
            match-page))

(defn parse-double-player [double-player-snippet]
  [(->> double-player-snippet
        second
        :content
        first)
   (->> double-player-snippet
        (#(nth % 4))
        :content
        first)])

(defn parse-single-player [single-player-snippet]
  [(->> single-player-snippet
        second
        :content
        first)])

(defn parse-player [player-snippet]
  (if (= 6 (count player-snippet))
    (parse-double-player player-snippet)
    (parse-single-player player-snippet)))

(defn parse-scores-from-score-array [scores-snippet]
  (if (nil? scores-snippet)
    [nil nil]
    (->> scores-snippet
         first
         (#(str/split % #":"))
         (map #(Integer/parseInt %)))))

(defn no-images? [game-snippet]
  (= 9 (count game-snippet)))

(defn parse-home-players [game-snippet]
  (->> game-snippet
       (#(nth % (if (no-images? game-snippet)
                  3
                  5)))
       :content
       parse-player))

(defn parse-guest-players [game-snippet]
  (->> game-snippet
       (#(nth % (if (no-images? game-snippet)
                  7
                  9)))
       :content
       parse-player))

(defn parse-scores [game-snippet]
  (->> game-snippet
       (#(nth % (if (no-images? game-snippet)
                  5
                  7)))
       :content
       parse-scores-from-score-array))

(defn parse-game [parsed-html]
  (let [game-snippet (:content parsed-html)
        position (->> game-snippet
                      second
                      :content
                      first
                      Integer/parseInt)
        home-player (parse-home-players game-snippet)
        guest-player (parse-guest-players game-snippet)
        scores (parse-scores game-snippet)]
    {:home     {:names home-player
                :score (first scores)}
     :guest    {:names guest-player
                :score (second scores)}
     :position position}))

(defn parse-games [parsed-html]
  (let [game-snippets (find-game-snippets parsed-html)]
    (map parse-game game-snippets)))

(defn parse-teams [match-page]
  (let [teams-snippet (s/select (s/descendant (s/and (s/class "sectiontableheader")
                                                     (s/tag :th)))
                                match-page)
        teams-string (->> teams-snippet
                          second
                          :content
                          (#(nth % 6)))
        teams (->> teams-string
                   (#(str/replace % #">" ""))
                   str/trim
                   (#(str/split % #"vs.")))]
    {:home-team  (first teams)
     :guest-team (second teams)}))

(defn parse-link [match-page]
  (let [link-snippet (s/select (s/descendant (s/tag :link))
                               match-page)]
    (-> link-snippet
        (nth 7)
        :attrs
        :href)))

(defn reformat-date [date-string]
  (.format
    (java.text.SimpleDateFormat. "yyyy-MM-dd")
    (.parse
      (java.text.SimpleDateFormat. "dd.MM.yyyy")
      date-string)))

(defn parse-date [match-page]
  (let [date-snippet (s/select (s/descendant (s/and (s/class "uk-overflow-auto")
                                                    (s/tag :div)))
                               match-page)
        date-string (->> date-snippet
                         first
                         :content
                         first
                         :content
                         second
                         :content
                         first
                         :content
                         second
                         :content
                         first)
        cleaned-date-string (->> date-string
                                 str/trim
                                 (#(str/split % #" "))
                                 second)]
    (reformat-date cleaned-date-string)))

(defn parse-match-day [match-page]
  (let [date-snippet (s/select (s/descendant (s/and (s/class "uk-overflow-auto")
                                                    (s/tag :div)))
                               match-page)
        date-string (->> date-snippet
                         first
                         :content
                         first
                         :content
                         second
                         :content
                         first
                         :content
                         second
                         :content
                         first)
        match-day (->> date-string
                       str/trim
                       (#(str/split % #" "))
                       (#(nth % 3))
                       (#(str/replace % #"\." ""))
                       Integer/parseInt)]
    match-day))

(defn valid-match? [match-page]
  (let [date-snippet (s/select (s/descendant (s/and (s/class "uk-overflow-auto")
                                                    (s/tag :div)))
                               match-page)
        date-string (->> date-snippet
                         first
                         :content
                         first
                         :content
                         second
                         :content
                         first
                         :content
                         second
                         :content
                         first)
        cleaned-date-string (->> date-string
                                 str/trim)]
    (not (str/starts-with? cleaned-date-string "N/A"))))

(defn parse-match [match-page]
  (let [date (parse-date match-page)
        teams (parse-teams match-page)
        games (parse-games match-page)
        link (parse-link match-page)
        match-day (parse-match-day match-page)]
    {:date       date
     :home-team  (:home-team teams)
     :guest-team (:guest-team teams)
     :games      games
     :link       link
     :match-day  match-day}))

(defn parse-valid-match [match-page]
  (if (valid-match? match-page)
    (parse-match match-page)
    nil))

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

(defn match->edn-file!
  ([match]
   (match->edn-file! default-downloaded-matches-directory match))
  ([path match]
   (let [filename (->> match
                       :link
                       link->filename)
         path (str path "/" filename)]
     (io/make-parents path)
     (spit path
           (clojure.core/pr-str match)))))

(defn matches->edn-files! [matches]
  (doseq [match matches]
    (match->edn-file! match)))

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

(defn log-parsing-link [link]
  (prn (str "parsing " link))
  link)

(defn log-matches-count [matches]
  (prn (str "matches found: " (count matches)))
  matches)

(defn log-new-matches-count [matches]
  (prn (str "new matches found: " (count matches)))
  matches)

(defn log-valid-matches-count [matches]
  (prn (str "valid matches found: " (count matches)))
  matches)

(defn log-parsed-matches-count [matches]
  (prn (str "new matches parsed: " (count matches)))
  matches)

(defn new-match?
  ([link]
   (new-match? default-downloaded-matches-directory link))
  ([directory link]
   (let [filename (link->filename link)]
     (not (.exists
            (io/file (str directory "/" filename)))))))

(def parse-match-from-link-fn (comp
                                parse-valid-match
                                html->hickory
                                log-parsing-link))

(defn load-season [{:keys [season match-directory-path]
                    :as   options}]
  (prn "downloading matches ..")
  (prn "options: " options)
  (->> season
       get-season
       get-league-links-from-league-overview
       (map html->hickory)
       (map get-match-links-from-league)
       flatten
       log-matches-count
       (filter new-match?)
       log-new-matches-count
       (map parse-match-from-link-fn)
       log-parsed-matches-count
       (filter some?)
       log-valid-matches-count
       (matches->edn-files!)))

(def cli-config
  {:app         {:command     "kicker-league-scanner"
                 :description "A command-line kicker stats scanner"
                 :version     "0.0.1"}
   :global-opts [{:option  "match-directory-path"
                  :short   "mdp"
                  :as      (str "Location of all matches. e.g. " default-downloaded-matches-directory)
                  :type    :string
                  :default default-downloaded-matches-directory}]
   :commands    [{:command     "download" :short "d"
                  :description ["downloads all matches for the given season"]
                  :opts        [{:option  "season"
                                 :short   "s"
                                 :as      "target season"
                                 :type    :string
                                 :default current-season}]
                  :runs        load-season}
                 {:command     "export" :short "s"
                  :description "exports all matches to a given csv file"
                  :opts        [{:option  "target-csv-file"
                                 :short   "tcf"
                                 :as      (str "Location for the csv file with all games. e.g. " default-csv-file-path)
                                 :type    :string
                                 :default default-csv-file-path}]
                  :runs        save-all-matches-to-csv}]})
;TODO: move io files to different namespace

;TODO: change author
;  howto: https://gist.github.com/amalmurali47/77e8dc1f27c791729518701d2dec3680
(defn -main [& args]
  (cli/run-cmd args cli-config)
  (comment
    (load-season {:match-directory-path default-downloaded-matches-directory
                  :season               current-season})
    (save-all-matches-to-csv {:match-directory-path default-downloaded-matches-directory
                              :target-csv-file      default-csv-file-path})))

