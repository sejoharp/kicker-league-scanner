(ns kicker-league-scanner.cli
  (:require [java-time.api :as jt]
            [kicker-league-scanner.http-server :as http-server]
            [kicker-league-scanner.io :as io]))

(def default-downloaded-matches-directory "downloaded-matches")

(def now-in-readable-format
  (jt/format "YYYY-MM-dd" (jt/local-date-time)))

(def default-csv-file-name
  (str "all-games-" now-in-readable-format ".csv.bz2"))

(def default-csv-file-path
  (str "./" default-csv-file-name))

(defn create-cli-config []
  {:command     "kicker-league-scanner"
   :description "A command-line kicker stats scanner"
   :subcommands [{:command     "download" :short "d"
                  :description ["downloads all matches for the given season"]
                  :opts        [{:option  "season"
                                 :short   "s"
                                 :as      "target season"
                                 :type    :string
                                 :default io/current-season}
                                {:option  "match-directory-path"
                                 :short   "mdp"
                                 :as      "Location of all matches."
                                 :type    :string
                                 :default default-downloaded-matches-directory}]
                  :runs        io/load-season!}
                 {:command     "export" :short "e"
                  :description "exports all matches to a given csv file"
                  :opts        [{:option  "target-csv-file"
                                 :short   "tcf"
                                 :as      "Location for the csv file with all games."
                                 :type    :string
                                 :default default-csv-file-path}
                                {:option  "match-directory-path"
                                 :short   "mdp"
                                 :as      "Location of all matches."
                                 :type    :string
                                 :default default-downloaded-matches-directory}]
                  :runs        io/save-all-matches-to-csv-file}
                 {:command     "upload" :short "u"
                  :description "uploads all matches to nextcloud"
                  :opts        [{:option "target-domain"
                                 :short  "td"
                                 :as     "target domain"
                                 :type   :string
                                 :env    "KICKER_TARGET_DOMAIN"}
                                {:option "target-user"
                                 :short  "tu"
                                 :as     "target user"
                                 :type   :string
                                 :env    "KICKER_TARGET_USER"}
                                {:option "target-password"
                                 :short  "tp"
                                 :as     "target password"
                                 :type   :string
                                 :env    "KICKER_TARGET_PASSWORD"}
                                {:option  "match-directory-path"
                                 :short   "mdp"
                                 :as      "Location of all matches."
                                 :type    :string
                                 :default default-downloaded-matches-directory}]
                  :runs        io/upload-all-matches-to-nextcloud!}
                 {:command     "server" :short "s"
                  :description "uploads all matches to nextcloud"
                  :opts        [{:option "target-domain"
                                 :short  "td"
                                 :as     "target domain"
                                 :type   :string
                                 :env    "KICKER_TARGET_DOMAIN"}
                                {:option "target-user"
                                 :short  "tu"
                                 :as     "target user"
                                 :type   :string
                                 :env    "KICKER_TARGET_USER"}
                                {:option "target-password"
                                 :short  "tp"
                                 :as     "target password"
                                 :type   :string
                                 :env    "KICKER_TARGET_PASSWORD"}
                                {:option  "match-directory-path"
                                 :short   "mdp"
                                 :as      "Location of all matches."
                                 :type    :string
                                 :default default-downloaded-matches-directory}]
                  :runs        http-server/start-server-blocking-mode}]})
