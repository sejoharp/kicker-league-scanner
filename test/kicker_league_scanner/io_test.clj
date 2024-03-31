(ns kicker-league-scanner.io-test
  (:require [clojure.test :refer :all]
            [kicker-league-scanner.io :as io]))

(deftest checks-file-existence
  (is (false?
        (io/new-match? "test/resources" "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=237&id=15478")))
  (is (true?
        (io/new-match? "test/resources" "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=237&id=non-existant"))))

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
    (io/delete-file path)
    (io/matches->csv-file! path [match])
    (is (= expected-csv-content
           (io/read-match-from-csv path)))
    (io/delete-file path)))

(deftest saves-match-to-edn-file
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}
        path "test/resources/task=begegnung_spielplan-veranstaltungid=229-id=15012.edn"]
    (io/delete-file path)
    (io/match->edn-file! "test/resources/" match)
    (is (= match
           (io/read-match-from-edn path)))
    (io/delete-file path)))

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
           (first (io/match->csv match))))
    (is (= "2023-09-05;1;4;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR)"
           (nth (io/match->csv match) 3)))))

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
           (io/game->csv match single-game)))
    (is (= "2023-09-05;1;4;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR)"
           (io/game->csv match double-game)))))

(deftest transforms-link-to-filename
  (is (= "task=begegnung_spielplan-veranstaltungid=237-id=15478.edn"
         (io/link->filename "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=237&id=15478"))))
