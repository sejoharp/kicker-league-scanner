(ns kicker-league-scanner.core
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hickory.core :as h]
            [hickory.select :as s])

  (:gen-class))

(defn html->hickory [overview-link]
  (let [html (slurp overview-link)]
    (h/as-hickory (h/parse html))))

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

(defn get-season [season]
  (->> {:form-params {:filter_saison_id (get season-year->id season)
                      :ok               "Los"
                      :task             "veranstaltungen"}}
       (client/post league-overview-season-link)
       :body
       h/parse
       h/as-hickory))

(def downloaded-matches-directory "downloaded-matches")
(def csv-file-path "./all-games.csv")

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
  (->> scores-snippet
       first
       (#(str/split % #":"))
       (map #(Integer/parseInt %))))

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

(defn match->csv-file!
  ([match]
   (match->csv-file! csv-file-path match))
  ([file-path match]
   (io/make-parents file-path)
   (doseq [game-string (match->csv match)]
     (spit file-path
           (str game-string "\n")
           :append true))))

(defn link->filename [link]
  (-> link
      (#(str/split % #"\?"))
      second
      (#(str/replace % #"&" "-"))
      (#(str % ".edn"))))

(defn match->edn-file!
  ([match]
   (match->edn-file! downloaded-matches-directory match))
  ([path match]
   (let [filename (->> match
                       :link
                       link->filename)
         path (str path "/" filename)]
     (io/make-parents path)
     (spit path
           (clojure.core/pr-str match)))))

(defn read-match-as-edn [file-path]
  (->> file-path
       slurp
       read-string))

(defn read-match-as-csv [file-path]
  (->> file-path
       slurp))

(defn delete-file [path]
  (io/delete-file path true))

(defn persist-match! [match]
  (match->edn-file! match)
  (match->csv-file! match))

(defn log [link]
  (prn (str "parsing " link))
  link)

(defn new-match?
  ([link]
   (new-match? downloaded-matches-directory link))
  ([directory link]
   (let [filename (link->filename link)]
     (not (.exists
           (io/file (str directory "/" filename)))))))

(def parse-match-from-link-fn (comp parse-match html->hickory log))

(defn load-season [season]
  (->> season
       get-season
       get-league-links-from-league-overview
       (map html->hickory)
       (map get-match-links-from-league)
       flatten
       (filter new-match?)
       (map parse-match-from-link-fn)
       ;TODO: map does not make sense, because the function does not return something new
       ;  use doseq?
       (map persist-match!)))

;TODO: change author
; howto: https://gist.github.com/amalmurali47/77e8dc1f27c791729518701d2dec3680
(defn -main []
  (println "Hello, World!")
  (comment
    (load-season "2023/24")))

