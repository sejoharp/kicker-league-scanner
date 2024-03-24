(ns kicker-league-scanner.core-test
  (:require [clojure.test :refer :all]
            [kicker-league-scanner.core :refer :all]))

(deftest html->hickory-test
  (let [parsed-html (html->hickory "test/resources/league.html")]
    (is (= :document
           (:type parsed-html)))))

(deftest parses-leagues-from-overview
  (let [parsed-html (html->hickory "test/resources/league-overview.html")]
    (is (= ["/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=228"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=229"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=230"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=231"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=232"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=233"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=234"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=235"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=236"
            "/liga/ergebnisse-und-tabellen?task=veranstaltung&veranstaltungid=237"]
           (get-league-links-from-league-overview parsed-html)))))

(deftest completed-match-test
  (is (= true
         (completed-match? ["28:4"])))
  (is (= false
         (completed-match? ["_:_"]))))

(deftest parses-matches-from-league
  (let [parsed-html (html->hickory "test/resources/league.html")]
    (is (= ["/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15018"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15014"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15020"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15016"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15024"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15030"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15026"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15028"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15022"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15034"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15032"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15040"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15038"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15036"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15044"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15042"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15050"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15046"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15048"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15054"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15058"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15056"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15052"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15060"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15062"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15070"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15066"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15068"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15064"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15072"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15076"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15080"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15078"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15074"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15084"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15090"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15086"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15088"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15082"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15096"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15092"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15100"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15098"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15094"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15019"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15013"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15015"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15021"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15017"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15025"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15023"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15029"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15031"
            "/liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15027"]

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
           (get-games-from-match parsed-html)))))

(deftest parses-teams
  (let [match-html (html->hickory "test/resources/match.html")]
    (is (= {:home-team  "Flying Circus"
            :guest-team "Kickertrupp (NR)"}
           (parse-teams match-html)))))

(deftest parses-date
  (let [match-html (html->hickory "test/resources/match.html")]
    (is (= "2023-09-05"
           (parse-date match-html)))))

