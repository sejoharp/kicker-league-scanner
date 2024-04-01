(ns kicker-league-scanner.core
  (:require [cli-matic.core :as cli]
            [clojure.string :as str]
            [hickory.select :as s]
            [kicker-league-scanner.io :as io])

  (:gen-class))


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

(def parse-match-from-link-fn (comp
                                parse-valid-match
                                io/html->hickory
                                log-parsing-link))

(defn load-season [{:keys [season match-directory-path]
                    :as   options}]
  (prn "downloading matches ..")
  (prn "options: " options)
  (->> season
       io/get-season
       get-league-links-from-league-overview
       (map io/html->hickory)
       (map get-match-links-from-league)
       flatten
       log-matches-count
       (filter (partial io/new-match? match-directory-path))
       log-new-matches-count
       (map parse-match-from-link-fn)
       log-parsed-matches-count
       (filter some?)
       log-valid-matches-count
       (partial io/matches->edn-files! match-directory-path)))

(def cli-config
  {:app         {:command     "kicker-league-scanner"
                 :description "A command-line kicker stats scanner"
                 :version     "0.0.1"}
   :global-opts [{:option  "match-directory-path"
                  :short   "mdp"
                  :as      (str "Location of all matches.")
                  :type    :string
                  :default io/default-downloaded-matches-directory}]
   :commands    [{:command     "download" :short "d"
                  :description ["downloads all matches for the given season"]
                  :opts        [{:option  "season"
                                 :short   "s"
                                 :as      "target season"
                                 :type    :string
                                 :default io/current-season}]
                  :runs        load-season}
                 {:command     "export" :short "s"
                  :description "exports all matches to a given csv file"
                  :opts        [{:option  "target-csv-file"
                                 :short   "tcf"
                                 :as      (str "Location for the csv file with all games.")
                                 :type    :string
                                 :default io/default-csv-file-path}]
                  :runs        io/save-all-matches-to-csv}]})

;TODO: change author
;  howto: https://gist.github.com/amalmurali47/77e8dc1f27c791729518701d2dec3680
(defn -main [& args]
  (cli/run-cmd args cli-config)
  (comment
    (load-season {:match-directory-path io/default-downloaded-matches-directory
                  :season               io/current-season})
    (io/save-all-matches-to-csv {:match-directory-path io/default-downloaded-matches-directory
                              :target-csv-file      io/default-csv-file-path})))

