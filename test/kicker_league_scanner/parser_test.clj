(ns kicker-league-scanner.parser-test
  (:require [clojure.test :refer :all])
  (:require [clojure.string :as str]
            [kicker-league-scanner.io :as io]
            [kicker-league-scanner.parser :as parser]))

(deftest html->hickory-test
  (let [parsed-html (io/html->hickory "test/resources/league.html")]
    (is (= :document
           (:type parsed-html)))))

(deftest parses-leagues-from-overview
  (let [parsed-html (io/html->hickory "test/resources/league-overview.html")]
    (is (= ["https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=228"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=229"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=230"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=231"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=232"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=233"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=234"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=235"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=236"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=237"]
           (parser/get-league-links-from-league-overview parsed-html)))))

(deftest completed-match-test
  (is (= true
         (parser/completed-match? ["28:4"])))
  (is (= false
         (parser/completed-match? ["_:_"]))))

(deftest parses-matches-from-league
  (let [parsed-html (io/html->hickory "test/resources/league.html")]
    (is (= ["https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15018"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15014"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15020"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15016"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15024"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15030"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15026"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15028"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15022"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15034"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15032"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15040"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15038"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15036"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15044"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15042"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15050"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15046"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15048"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15054"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15058"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15056"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15052"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15060"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15062"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15070"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15066"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15068"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15064"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15072"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15076"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15080"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15078"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15074"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15084"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15090"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15086"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15088"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15082"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15096"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15092"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15100"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15098"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15094"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15019"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15013"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15015"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15021"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15017"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15025"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15023"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15029"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15031"
            "https://kickern-hamburg.de/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15027"]

           (parser/get-match-links-from-league parsed-html)))))

(deftest parses-game
  (let [game-snippets (parser/find-game-snippets (io/html->hickory "test/resources/match.html"))
        single-game (first game-snippets)
        double-game (nth game-snippets 2)]
    (is (= {:home     {:names ["Felix"] :score 6}
            :guest    {:names ["Samuel"] :score 2}
            :position 1}
           (parser/parse-game single-game)))
    (is (= {:home     {:names ["Walter" "Felix"] :score 3}
            :guest    {:names ["Samuel" "Boran"] :score 6}
            :position 3}
           (parser/parse-game double-game)))))

(deftest parses-game-without-scores
  (let [game-snippets (parser/find-game-snippets (io/html->hickory "test/resources/match-missing-scores.html"))
        single-game (first game-snippets)
        double-game (nth game-snippets 2)]
    (is (= {:home     {:names ["Wendy"] :score nil}
            :guest    {:names ["Thomas"] :score nil}
            :position 1}
           (parser/parse-game single-game)))
    (is (= {:home     {:names ["Olivia" "Henry"] :score nil}
            :guest    {:names ["Thomas" "Carter"] :score nil}
            :position 3}
           (parser/parse-game double-game)))))
(def game-without-images {:attrs   {:class "sectiontableentry1"},
                          :content ["\n                                "
                                    {:attrs   {:align "center", :nowrap ""},
                                     :content ["1"],
                                     :tag     :td,
                                     :type    :element}
                                    "\n\n                                "
                                    {:attrs   nil,
                                     :content ["\n                                    "
                                               {:attrs   {:href "/liga/ergebnisse-und-tabellen?task=spieler_details&id=2331"},
                                                :content ["Isla"],
                                                :tag     :a,
                                                :type    :element}
                                               "\n                                "],
                                     :tag     :td,
                                     :type    :element}
                                    "\n\n                                "
                                    {:attrs   {:align "center", :nowrap ""},
                                     :content ["6:1"],
                                     :tag     :td,
                                     :type    :element}
                                    "\n\n                                "
                                    {:attrs   nil,
                                     :content ["\n                                    "
                                               {:attrs   {:href "/liga/ergebnisse-und-tabellen?task=spieler_details&id=1040"},
                                                :content ["Alice"],
                                                :tag     :a,
                                                :type    :element}
                                               "\n                                "],
                                     :tag     :td,
                                     :type    :element}
                                    "\n\n                            "],
                          :tag     :tr,
                          :type    :element})

(deftest parses-game-without-images
  (is (= {:home     {:names ["Isla"] :score 6}
          :guest    {:names ["Alice"] :score 1}
          :position 1}
         (parser/parse-game game-without-images))))

(deftest parses-games-from-match
  (let [parsed-html (io/html->hickory "test/resources/match.html")]
    (is (= [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
            {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2}
            {:home {:names ["Walter" "Felix"] :score 3} :guest {:names ["Samuel" "Boran"] :score 6} :position 3}
            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}
            {:home {:names ["George"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 5}
            {:home {:names ["Walter"] :score 6} :guest {:names ["Leon"] :score 3} :position 6}
            {:home {:names ["George" "Ava"] :score 2} :guest {:names ["Yasmine" "Boran"] :score 6} :position 7}
            {:home {:names ["George" "Ava"] :score 2} :guest {:names ["Yasmine" "Boran"] :score 6} :position 8}
            {:home {:names ["Felix"] :score 6} :guest {:names ["Ian"] :score 4} :position 9}
            {:home {:names ["Ava"] :score 6} :guest {:names ["Leon"] :score 3} :position 10}
            {:home {:names ["Walter" "Ava"] :score 3} :guest {:names ["Ian" "Derek"] :score 6} :position 11}
            {:home {:names ["Walter" "Ava"] :score 5} :guest {:names ["Ian" "Derek"] :score 5} :position 12}
            {:home {:names ["George"] :score 6} :guest {:names ["Samuel"] :score 3} :position 13}
            {:home {:names ["Ava"] :score 6} :guest {:names ["Ian"] :score 4} :position 14}
            {:home {:names ["George" "Felix"] :score 2} :guest {:names ["Samuel" "Derek"] :score 6} :position 15}
            {:home {:names ["George" "Felix"] :score 6} :guest {:names ["Samuel" "Derek"] :score 2} :position 16}]
           (parser/parse-games parsed-html)))))

(deftest parses-all-games-without-images
  (let [games-without-images (io/html->hickory "test/resources/match-with-unexpected-missing-games.html")]
    (is (= {:guest    {:names ["Alice" "Black"], :score 2},
            :home     {:names ["Isla" "Bella"], :score 6},
            :position 3}
           (nth (parser/parse-games games-without-images) 2)))))

(deftest parses-teams
  (let [match-html (io/html->hickory "test/resources/match.html")]
    (is (= {:home-team  "Flying Circus"
            :guest-team "Kickertrupp (NR)"}
           (parser/parse-teams match-html)))))

(deftest parses-date
  (let [match-html (io/html->hickory "test/resources/match.html")]
    (is (= "2023-09-05"
           (parser/parse-date match-html)))))

(deftest parses-match-day
  (let [match-html (io/html->hickory "test/resources/match.html")]
    (is (= 1
           (parser/parse-match-day match-html)))))

(deftest parses-match-day-without-game-day
  (let [match-html (io/html->hickory "test/resources/match-without-game-day-relegation.html")]
    (is (= nil
           (parser/parse-match-day match-html)))))

(deftest parses-link
  (let [match-html (io/html->hickory "test/resources/match.html")]
    (is (= "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
           (parser/parse-link match-html)))))

(deftest detects-invalid-matches
  (let [valid-match-html (io/html->hickory "test/resources/match.html")
        missing-date-match-html (io/html->hickory "test/resources/match-missing-date.html")
        match-unacknowledged-html (io/html->hickory "test/resources/match-unacknowledged.html")]
    (is (true? (parser/valid-match? valid-match-html)))
    (is (false? (parser/valid-match? missing-date-match-html)))
    (is (false? (parser/valid-match? match-unacknowledged-html)))))

(deftest parses-match
  (let [match-html (io/html->hickory "test/resources/match.html")]
    (is (= {:date       "2023-09-05"
            :home-team  "Flying Circus"
            :guest-team "Kickertrupp (NR)"
            :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                         {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2}
                         {:home {:names ["Walter" "Felix"] :score 3} :guest {:names ["Samuel" "Boran"] :score 6} :position 3}
                         {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}
                         {:home {:names ["George"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 5}
                         {:home {:names ["Walter"] :score 6} :guest {:names ["Leon"] :score 3} :position 6}
                         {:home {:names ["George" "Ava"] :score 2} :guest {:names ["Yasmine" "Boran"] :score 6} :position 7}
                         {:home {:names ["George" "Ava"] :score 2} :guest {:names ["Yasmine" "Boran"] :score 6} :position 8}
                         {:home {:names ["Felix"] :score 6} :guest {:names ["Ian"] :score 4} :position 9}
                         {:home {:names ["Ava"] :score 6} :guest {:names ["Leon"] :score 3} :position 10}
                         {:home {:names ["Walter" "Ava"] :score 3} :guest {:names ["Ian" "Derek"] :score 6} :position 11}
                         {:home {:names ["Walter" "Ava"] :score 5} :guest {:names ["Ian" "Derek"] :score 5} :position 12}
                         {:home {:names ["George"] :score 6} :guest {:names ["Samuel"] :score 3} :position 13}
                         {:home {:names ["Ava"] :score 6} :guest {:names ["Ian"] :score 4} :position 14}
                         {:home {:names ["George" "Felix"] :score 2} :guest {:names ["Samuel" "Derek"] :score 6} :position 15}
                         {:home {:names ["George" "Felix"] :score 6} :guest {:names ["Samuel" "Derek"] :score 2} :position 16}]
            :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
            :match-day  1}
           (parser/parse-match match-html)))))

(deftest detects-invalid-match
  (let [invalid-match-html (io/html->hickory "test/resources/match-missing-date.html")]
    (is (nil? (parser/parse-valid-match invalid-match-html)))))

(defn get-team-goals [games team]
  (map (fn [game]
         (get-in game [team :score])) games))

(defn missing-scores? [match]
  (let [home-goals (get-team-goals (:games match) :home)
        guest-goals (get-team-goals (:games match) :guest)]
    (> (count (filter nil? (concat home-goals guest-goals))) 0)
    ))


(defn match-score [{:keys [games] :as match}]
  (let [match-points (map (fn [game]
                            (let [home-score (get-in [:home :score] game)
                                  guest-score (get-in [:guest :score] game)]
                              (cond
                                (= home-score guest-score) 2
                                (> home-score guest-score) 2
                                :else 0)))
                          games)]
    (reduce + match-points)))

(defn not-32-points? [match]
  (not (= 32 (match-score match))))

(defn incomplete-match? [match]
  (println (:date match) (:link match))
  (when (str/starts-with? (:date match) "2024")
    (or
      (missing-scores? match)
      (not-32-points? match))))

; test do find incomplete written matches - it should be fixed by waiting until the matches are completed.
; in this came: not live or unacknowledged
#_(deftest finds-running-matches
  (let [all-matches (->> "downloaded-matches"
                         io/read-match-files
                         (map io/read-match-from-edn))
        incomplete-matches (doall (filter incomplete-match?
                                          all-matches))]
    (doseq [match incomplete-matches]
      (when (not (-> match
                     :games
                     first
                     :home
                     :names
                     first
                     nil?))
        (clojure.pprint/pprint match)
        (println (str "missing scores? " (missing-scores? match)))
        (println (str "not 32 points reached: " (not-32-points? match)))))

    #_(is (nil? (seq incomplete-matches)))))
