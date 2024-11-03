(ns kicker-league-scanner.http-server-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [org.httpkit.client :as http-client]
            [kicker-league-scanner.http-server :as server]))

(deftest create-status-handler-test
  (let [close-channel (async/chan)
        server (async/go (server/start-server close-channel {}))]
    (is (= 200 (:status @(http-client/get "http://localhost/status"))))
    (async/>!! close-channel "close")))
