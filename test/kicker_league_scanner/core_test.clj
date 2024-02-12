(ns kicker-league-scanner.core-test
  (:require [clojure.test :refer :all]
            [hickory.select :as s]
            [kicker-league-scanner.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 0))))

(deftest ^:unit ^:test-refresh/focus league-overview
  (let [parsed-html (read-league-overview "test/resources/league-overview.html")]
    (is (= :document
           (:type parsed-html)))
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
           (parse-game-links parsed-html)))))
