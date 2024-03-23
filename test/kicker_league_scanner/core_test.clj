(ns kicker-league-scanner.core-test
  (:require [clojure.test :refer :all]
            [kicker-league-scanner.core :refer :all]))

(deftest html->hickory-test
  (let [parsed-html (html->hickory "test/resources/league.html")]
    (is (= :document
           (:type parsed-html)))))

(deftest league-overview
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
(deftest ^:test-refresh/focus league-matches
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