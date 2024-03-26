(ns kicker-league-scanner.core-test
  (:require [clojure.test :refer :all]
            [kicker-league-scanner.core :refer :all]))

(deftest html->hickory-test
  (let [parsed-html (html->hickory "test/resources/league.html")]
    (is (= :document
           (:type parsed-html)))))

(deftest parses-leagues-from-overview
  (let [parsed-html (html->hickory "test/resources/league-overview.html")]
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
           (get-league-links-from-league-overview parsed-html)))))

(deftest completed-match-test
  (is (= true
         (completed-match? ["28:4"])))
  (is (= false
         (completed-match? ["_:_"]))))

(deftest parses-matches-from-league
  (let [parsed-html (html->hickory "test/resources/league.html")]
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

           (get-match-links-from-league parsed-html)))))

(deftest parses-game
  (let [game-snippets (find-game-snippets (html->hickory "test/resources/match.html"))
        single-game (first game-snippets)
        double-game (nth game-snippets 2)]
    (is (= {:home     {:names ["Felix"] :score 6}
            :guest    {:names ["Samuel"] :score 2}
            :position 1}
           (parse-game single-game)))
    (is (= {:home     {:names ["Walter" "Felix"] :score 3}
            :guest    {:names ["Samuel" "Boran"] :score 6}
            :position 3}
           (parse-game double-game)))))

(deftest parses-games-from-match
  (let [parsed-html (html->hickory "test/resources/match.html")]
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
           (parse-games parsed-html)))))

(deftest parses-teams
  (let [match-html (html->hickory "test/resources/match.html")]
    (is (= {:home-team  "Flying Circus"
            :guest-team "Kickertrupp (NR)"}
           (parse-teams match-html)))))

(deftest parses-date
  (let [match-html (html->hickory "test/resources/match.html")]
    (is (= "2023-09-05"
           (parse-date match-html)))))

(deftest parses-match-day
  (let [match-html (html->hickory "test/resources/match.html")]
    (is (= 1
           (parse-match-day match-html)))))

(deftest parses-link
  (let [match-html (html->hickory "test/resources/match.html")]
    (is (= "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
           (parse-link match-html)))))

(deftest parses-match
  (let [match-html (html->hickory "test/resources/match.html")]
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
           (parse-match match-html)))))

(deftest checks-file-existence
  (is (false?
        (new-match? "test/resources" "task=begegnung_spielplan&veranstaltungid=237&id=15478.edn")))
  (is (true?
        (new-match? "test/resources" "non-existant-file.edn"))))

(deftest game->csv-test
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}
        single-game (first (:games match))
        double-game (second (:games match))]
    (is (= "2023-09-05;1;1;Flying Circus;Felix;XXXX;6;2;Samuel;XXXX;Kickertrupp (NR)"
           (game->csv match single-game)))
    (is (= "2023-09-05;1;4;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR)"
           (game->csv match double-game)))))

(deftest match->csv-test
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2}
                            {:home {:names ["Walter" "Felix"] :score 3} :guest {:names ["Samuel" "Boran"] :score 6} :position 3}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}]
    (is (= "2023-09-05;1;1;Flying Circus;Felix;XXXX;6;2;Samuel;XXXX;Kickertrupp (NR)"
           (first (match->csv match))))
    (is (= "2023-09-05;1;4;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR)"
           (nth (match->csv match) 3)))))

(deftest saves-match-to-csv-file
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}
        expected-csv-content "2023-09-05;1;1;Flying Circus;Felix;XXXX;6;2;Samuel;XXXX;Kickertrupp (NR)
2023-09-05;1;4;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR)
"
        path "test/resources/test-all-games.csv"]
    (delete-file path)
    (match->csv-file! path match)
    (is (= expected-csv-content
           (read-match-as-csv path)))
    (delete-file path)))

(deftest saves-match-to-edn-file
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}
        path "test/resources/task=begegnung_spielplan&veranstaltungid=229&id=15012.edn"]
    (delete-file path)
    (match->edn-file! "test/resources/" match)
    (is (= match
           (read-match-as-edn path)))
    (delete-file path)))