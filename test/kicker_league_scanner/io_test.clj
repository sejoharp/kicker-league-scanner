(ns kicker-league-scanner.io-test
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
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
        expected-csv-content [["2023-09-05" "1" "1" "H" "Flying Circus" "Felix" "XXXX" "6" "2" "Samuel" "XXXX" "Kickertrupp (NR)" "G" "2" "0" "2023/03" "1"]
                              ["2023-09-05" "1" "1" "H" "Flying Circus" "XXXX" "Felix" "6" "2" "XXXX" "Samuel" "Kickertrupp (NR)" "G" "2" "0" "2023/03" "1"]
                              ["2023-09-05" "1" "1" "G" "Kickertrupp (NR)" "Samuel" "XXXX" "2" "6" "Felix" "XXXX" "Flying Circus" "H" "0" "2" "2023/03" "1"]
                              ["2023-09-05" "1" "1" "G" "Kickertrupp (NR)" "XXXX" "Samuel" "2" "6" "XXXX" "Felix" "Flying Circus" "H" "0" "2" "2023/03" "1"]
                              ["2023-09-05" "1" "4" "H" "Flying Circus" "Walter" "Felix" "6" "4" "Samuel" "Boran" "Kickertrupp (NR)" "G" "2" "0" "2023/03" "1"]
                              ["2023-09-05" "1" "4" "H" "Flying Circus" "Felix" "Walter" "6" "4" "Boran" "Samuel" "Kickertrupp (NR)" "G" "2" "0" "2023/03" "1"]
                              ["2023-09-05" "1" "4" "G" "Kickertrupp (NR)" "Samuel" "Boran" "4" "6" "Walter" "Felix" "Flying Circus" "H" "0" "2" "2023/03" "1"]
                              ["2023-09-05" "1" "4" "G" "Kickertrupp (NR)" "Boran" "Samuel" "4" "6" "Felix" "Walter" "Flying Circus" "H" "0" "2" "2023/03" "1"]]
        path "test/resources/test-all-games.csv.bz2"]
    (io/delete-file path)
    (io/matches->csv-file! path [match])
    (is (= expected-csv-content
           (io/read-bzip2-as-string path)))
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


(deftest transforms-link-to-filename
  (is (= "task=begegnung_spielplan-veranstaltungid=237-id=15478.edn"
         (io/link->filename "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=237&id=15478"))))


(deftest reads-html-and-parses-match
  (let [match-link "test/resources/match.html"
        match (io/parse-match-from-link-fn match-link)]
    (is (true? (contains? match :date)))
    (is (true? (contains? match :home-team)))
    (is (true? (contains? match :guest-team)))
    (is (true? (contains? match :games)))
    (is (true? (contains? match :link)))
    (is (true? (contains? match :match-day)))
    (is (= 16 (count (:games match))))))

(deftest reads-html-and-parses-match-and-finds-all-games
  (let [match-link "test/resources/match-with-unexpected-missing-games.html"
        match (io/parse-match-from-link-fn match-link)]
    (is (true? (contains? match :date)))
    (is (true? (contains? match :home-team)))
    (is (true? (contains? match :guest-team)))
    (is (true? (contains? match :games)))
    (is (true? (contains? match :link)))
    (is (true? (contains? match :match-day)))
    (is (= 16 (count (:games match))))))
