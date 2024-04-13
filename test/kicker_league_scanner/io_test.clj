(ns kicker-league-scanner.io-test
  (:require [clojure.test :refer :all]
            [kicker-league-scanner.io :refer :all]
            [clojure.string :as str]))

(deftest checks-file-existence
  (is (false?
        (new-match? "test/resources" "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=237&id=15478")))
  (is (true?
        (new-match? "test/resources" "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=237&id=non-existant"))))

(deftest saves-match-to-csv-file
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}
        expected-csv-content (str (str/join "\n"
                                            ["2023-09-05;1;1;H;Flying Circus;Felix;XXXX;6;2;Samuel;XXXX;Kickertrupp (NR);G;2;0;2023/03;1"
                                             "2023-09-05;1;1;G;Kickertrupp (NR);Samuel;XXXX;2;6;Felix;XXXX;Flying Circus;H;0;2;2023/03;1"
                                             "2023-09-05;1;4;H;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR);G;2;0;2023/03;1"
                                             "2023-09-05;1;4;G;Kickertrupp (NR);Samuel;Boran;4;6;Walter;Felix;Flying Circus;H;0;2;2023/03;1"])
                                  "\n")
        path "test/resources/test-all-games.csv"]
    (delete-file path)
    (matches->csv-file! path [match])
    (is (= expected-csv-content
           (read-match-from-csv path)))
    (delete-file path)))

(deftest saves-match-to-edn-file
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}
        path "test/resources/task=begegnung_spielplan-veranstaltungid=229-id=15012.edn"]
    (delete-file path)
    (match->edn-file! "test/resources/" match)
    (is (= match
           (read-match-from-edn path)))
    (delete-file path)))

(deftest match->csv-test
  (let [match {:date       "2023-09-05"
               :home-team  "Flying Circus"
               :guest-team "Kickertrupp (NR)"
               :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                            {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2}
                            {:home {:names ["Walter" "Felix"] :score 3} :guest {:names ["Samuel" "Boran"] :score 6} :position 3}
                            {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
               :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
               :match-day  1}
        match-as-csv-lines (match->csv match)]
    (is (= "2023-09-05;1;4;H;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR);G;2;0;2023/03;1" (nth match-as-csv-lines 6)))
    (is (= "2023-09-05;1;4;G;Kickertrupp (NR);Samuel;Boran;4;6;Walter;Felix;Flying Circus;H;0;2;2023/03;1" (nth match-as-csv-lines 7)))))

(deftest calculate-quarter-test
  (is (= "2023/03" (calculate-quarter "2023-09-22")))
  (is (= "2023/01" (calculate-quarter "2023-01-02")))
  (is (= "2023/02" (calculate-quarter "2023-04-06")))
  (is (= "2023/04" (calculate-quarter "2023-10-11"))))

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
    (is (= ["2023-09-05;1;1;H;Flying Circus;Felix;XXXX;6;2;Samuel;XXXX;Kickertrupp (NR);G;2;0;2023/03;1"
            "2023-09-05;1;1;G;Kickertrupp (NR);Samuel;XXXX;2;6;Felix;XXXX;Flying Circus;H;0;2;2023/03;1"]
           (game->csv match single-game)))
    (is (= ["2023-09-05;1;4;H;Flying Circus;Walter;Felix;6;4;Samuel;Boran;Kickertrupp (NR);G;2;0;2023/03;1"
            "2023-09-05;1;4;G;Kickertrupp (NR);Samuel;Boran;4;6;Walter;Felix;Flying Circus;H;0;2;2023/03;1"]
           (game->csv match double-game)))))

(deftest transforms-link-to-filename
  (is (= "task=begegnung_spielplan-veranstaltungid=237-id=15478.edn"
         (link->filename "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=237&id=15478"))))


(deftest calculate-game-points-test
  (is (= [2 0]
         (calculate-game-points {:home {:names ["Walter"] :score 6} :guest {:names ["Yasmine"] :score 2} :position 2})))
  (is (= [0 2]
         (calculate-game-points {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2})))
  (is (= [1 1]
         (calculate-game-points {:home {:names ["Walter"] :score 5} :guest {:names ["Yasmine"] :score 5} :position 2})))
  (is (= [0 0]
         (calculate-game-points {:home {:names ["Walter"] :score 8} :guest {:names ["Yasmine"] :score 7} :position 2}))))

(deftest calculate-match-score-test
  (let [games [{:home {:names ["Felix"] :score 1} :guest {:names ["Samuel"] :score 6} :position 1}
               {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2}
               {:home {:names ["Walter" "Felix"] :score 3} :guest {:names ["Samuel" "Boran"] :score 6} :position 3}
               {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}
               {:home {:names ["Felix"] :score 2} :guest {:names ["Samuel"] :score 6} :position 5}
               {:home {:names ["Felix"] :score 2} :guest {:names ["Samuel"] :score 6} :position 6}]]
    (is (= [2 10]
           (calculate-match-score games)))))

(deftest calculate-points-test
  (let [match-guest-won {:date       "2023-09-05"
                         :home-team  "Flying Circus"
                         :guest-team "Kickertrupp (NR)"
                         :games      [{:home {:names ["Felix"] :score 1} :guest {:names ["Samuel"] :score 6} :position 1}
                                      {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2}
                                      {:home {:names ["Walter" "Felix"] :score 3} :guest {:names ["Samuel" "Boran"] :score 6} :position 3}
                                      {:home {:names ["Walter" "Felix"] :score 6} :guest {:names ["Samuel" "Boran"] :score 4} :position 4}]
                         :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
                         :match-day  1}
        match-tied {:date       "2023-09-05"
                    :home-team  "Flying Circus"
                    :guest-team "Kickertrupp (NR)"
                    :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                                 {:home {:names ["Walter"] :score 2} :guest {:names ["Yasmine"] :score 6} :position 2}]
                    :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
                    :match-day  1}
        match-home-won {:date       "2023-09-05"
                        :home-team  "Flying Circus"
                        :guest-team "Kickertrupp (NR)"
                        :games      [{:home {:names ["Felix"] :score 6} :guest {:names ["Samuel"] :score 2} :position 1}
                                     {:home {:names ["Walter"] :score 6} :guest {:names ["Yasmine"] :score 0} :position 2}]
                        :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"
                        :match-day  1}]
    (is (= [0 2]
           (calculate-points match-guest-won)))
    (is (= [1 1]
           (calculate-points match-tied)))
    (is (= [2 0]
           (calculate-points match-home-won)))))
