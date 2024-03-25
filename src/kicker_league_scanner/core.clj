(ns kicker-league-scanner.core
  (:require [clojure.string :as str]
            [hickory.core :as h]
            [hickory.select :as s])

  (:gen-class))

(defn html->hickory [overview-link]
  (let [html (slurp overview-link)]
    (h/as-hickory (h/parse html))))

; to select other seasons, do the following post request:
;   POST /liga/ergebnisse-und-tabellen HTTP/1.1
;   Host: example.com  (replace example.com with the actual host)
;   Content-Type: application/x-www-form-urlencoded
;   Content-Length: [length]
;
;   filter_saison_id=11&ok=Los&task=veranstaltungen
(def league-overview-link "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen")

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

(defn parse-scores [scores-snippet]
  (->> scores-snippet
       first
       (#(str/split % #":"))
       (map #(Integer/parseInt %))))

(defn parse-game [parsed-html]
  (let [game-snippet (:content parsed-html)
        position (->> game-snippet
                      second
                      :content
                      first
                      Integer/parseInt)
        home-player (->> game-snippet
                         (#(nth % 5))
                         :content
                         parse-player)
        guest-player (->> game-snippet
                          (#(nth % 9))
                          :content
                          parse-player)
        scores (->> game-snippet
                    (#(nth % 7))
                    :content
                    parse-scores)]

    ;#TODO: Brauche ich noch Datum und Teamnamen?
    ; Wie habe ich das bis jetzt gemacht?
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

(defn parse-match [match-page]
  (let [date (parse-date match-page)
        teams (parse-teams match-page)
        games (parse-games match-page)
        link (parse-link match-page)]
    {:date       date
     :home-team  (:home-team teams)
     :guest-team (:guest-team teams)
     :games      games
     :link link}))

(defn load-season [season-link]
  (->> season-link
       html->hickory
       get-league-links-from-league-overview
       (map html->hickory)
       (map get-match-links-from-league)
       flatten
       ;TODO: check if match already exists
       ;  identifier could be part of the link:
       ;  /liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15021
       (map html->hickory)
       (map parse-match)
       flatten
       ; persist each game as a file. The name is the parameter part of its link
       ))

(defn -main []
  (println "Hello, World!")
  (comment
    (load-season league-overview-link)))

