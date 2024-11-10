(ns kicker-league-scanner.core
  (:require [cli-matic.core :as cli-matic]
            [kicker-league-scanner.cli :as cli]
            [kicker-league-scanner.io :as io])
  (:gen-class))

(defn -main [& args]
  (cli-matic/run-cmd args (cli/create-cli-config)))

