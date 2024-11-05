(ns kicker-league-scanner.http-server-test
  (:require [clojure.core.async :as async]
            [clojure.data.json :as json]
            [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [kicker-league-scanner.http-server :as server]
            [org.httpkit.client :as http-client]))

(deftest create-status-handler-test
  (let [server (server/start-server {})]
    (try
      (let [response @(http-client/get "http://localhost/status")
            body (json/read-str (:body response))]
        (is (= 200 (:status response)))
        (is (= {"found-matches"  nil,
                "last-run"       nil,
                "new-matches"    nil,
                "parsed-matches" nil,
                "valid-matches"  nil}
               body)))
      (catch Exception e
        (println "test failed" e))
      (finally (server/stop-server server)))))

(deftest updates-status-after-downloading-new-matches
  (let [server (server/start-server {})]
    (try
      (loop [retries 5]
        (let [response @(http-client/get "http://localhost/status")
              body (json/read-str (:body response))]
          (is (= 200 (:status response)))
          (is (= {"found-matches"  nil,
                  "last-run"       nil,
                  "new-matches"    nil,
                  "parsed-matches" nil,
                  "valid-matches"  nil}
                 body)))
        (when (> retries 0)
          (recur (- retries 1))))
      (catch Exception e
        (println "test failed" e))
      (finally (server/stop-server server)))))
