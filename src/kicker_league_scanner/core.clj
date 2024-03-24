(ns kicker-league-scanner.core
  (:require [clojure.string :as str]
            [hickory.core :as h]
            [hickory.select :as s])

  (:gen-class))

(defn html->hickory [overview-link]
  (let [html (slurp overview-link)]
    (h/as-hickory (h/parse html))))

(defn get-league-overview [])

(defn get-league-links-from-league-overview [parsed-html]
  (let [link-htmls (s/select (s/descendant (s/class "readon"))
                             parsed-html)
        begegnungen-links (filter #(str/includes? (:content %) "Begegnungen …")
                                  link-htmls)
        links (map #(get-in % [:attrs :href])
                   begegnungen-links)]
    links))

(defn completed-match? [[result-info & remaining]]
  (let [first-character (first result-info)]
    (contains? (set "0123456789") first-character)))

(defn get-match-links-from-league [parsed-html]
  (let [link-maps (s/select (s/descendant (s/or (s/class "sectiontableentry1")
                                                (s/class "sectiontableentry2"))
                                          (s/tag :a))
                            parsed-html)]
    (->> link-maps
         (filter #(and
                    (completed-match? (:content %))
                    (some? (get-in % [:attrs :href]))
                    (str/includes? (get-in % [:attrs :href])
                                   "begegnung_spielplan")))
         (map #(get-in % [:attrs :href])))))

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
  (let [body (:content parsed-html)
        position (->> body
                      second
                      :content
                      first
                      Integer/parseInt)
        home-player (->> body
                         (#(nth % 5))
                         :content
                         parse-player)
        guest-player (->> body
                          (#(nth % 9))
                          :content
                          parse-player)
        scores (->> body
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

(defn get-games-from-match [parsed-html]
  (let [game-snippets (find-game-snippets parsed-html)]
    (->> game-snippets
         (map parse-game))))

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
        cleaned-date-string
        (->> date-string
             str/trim
             (#(str/split % #" "))
             second)]
    (reformat-date cleaned-date-string)))

(defn -main []
  (println "Hello, World!"))

