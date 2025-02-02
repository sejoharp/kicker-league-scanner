(ns kicker-league-scanner.parser
  (:require [clojure.string :as str]
            [hickory.select :as s])
  (:import (java.time ZonedDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

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

(defn safe-game-day [[weekday date time day :as _date-time-game-day-string]]
  (some->> day
           (#(str/replace % #"\." ""))
           Integer/parseInt))

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
                       safe-game-day)]
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
    (and (not (str/starts-with? cleaned-date-string "N/A"))
         (not (str/includes? cleaned-date-string "(Ergebnis unbestätigt)"))
         (not (str/includes? cleaned-date-string "live")))))

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

(defn log-matches-count [matches]
  (prn (str "matches found: " (count matches)))
  matches)

(defn current-user-friendly-timestamp []
  (let [now (ZonedDateTime/now (ZoneId/of "UTC")) ; You can change "UTC" to your desired time zone
        formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss z")]
    (.format now formatter)))
