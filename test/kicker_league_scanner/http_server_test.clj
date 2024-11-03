(ns kicker-league-scanner.http-server-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [kicker-league-scanner.http-server :as server]
            [org.httpkit.client :as http-client]))

(deftest create-status-handler-test
  (let [close-channel (async/chan)
        server (async/go (server/start-server close-channel {}))
        response @(http-client/get "http://localhost/status")]
    (is (= 200 (:status response)))
    (async/>!! close-channel "close")))
