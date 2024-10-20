(ns kicker-league-scanner.cli
  (:require [java-time.api :as jt]
            [kicker-league-scanner.io :as io]
            [lein-project-reader.core :as lpr]))

(def default-downloaded-matches-directory "downloaded-matches")

(def now-in-readable-format
  (jt/format "YYYY-MM-dd" (jt/local-date-time)))

(def default-csv-file-path
  (str "./all-games-" now-in-readable-format ".csv.bz2"))

(defn create-cli-config [load-season-fn]
  {:app         {:command     "kicker-league-scanner"
                 :description "A command-line kicker stats scanner"
                 :version     (:version (lpr/read-project))}
   :global-opts [{:option  "match-directory-path"
                  :short   "mdp"
                  :as      (str "Location of all matches.")
                  :type    :string
                  :default default-downloaded-matches-directory}]
   :commands    [{:command     "download" :short "d"
                  :description ["downloads all matches for the given season"]
                  :opts        [{:option  "season"
                                 :short   "s"
                                 :as      "target season"
                                 :type    :string
                                 :default io/current-season}]
                  :runs        load-season-fn}
                 {:command     "export" :short "s"
                  :description "exports all matches to a given csv file"
                  :opts        [{:option  "target-csv-file"
                                 :short   "tcf"
                                 :as      (str "Location for the csv file with all games.")
                                 :type    :string
                                 :default default-csv-file-path}]
                  :runs        io/save-all-matches-to-csv}]})
