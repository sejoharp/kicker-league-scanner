(ns kicker-league-scanner.http-server-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [kicker-league-scanner.http-server :as server]
            [org.httpkit.client :as http-client]))

(defn scheduled-fn-mock [app-status] (reset! app-status {:found-match-count  1
                                                         :new-match-count    1
                                                         :parsed-match-count 1
                                                         :valid-match-count  1
                                                         :valid-matches [{:date       "2023-09-05"
                                                                          :home-team  "Flying Circus"
                                                                          :guest-team "Kickertrupp (NR)"
                                                                          :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"}]
                                                         :last-run           0}))
(deftest shows-initial-status-content-test
  (let [server (server/start-server {:scheduled-fn       scheduled-fn-mock
                                     :scheduled-interval 5000})]
    (try
      (let [response @(http-client/get "http://localhost/status")
            body (json/read-str (:body response) {:key-fn keyword})]
        (is (= 200 (:status response)))
        (is (= {:found-match-count  nil
                :last-run           nil
                :new-match-count    nil
                :parsed-match-count nil
                :valid-match-count  nil
                :valid-matches  nil}
               body)))
      (catch Exception e
        (println "test failed" e))
      (finally (server/stop-server server)))))

(deftest updates-status-after-downloading-new-matches
  (let [server (server/start-server {:scheduled-fn       scheduled-fn-mock
                                     :scheduled-interval 1})]
    (try
      (loop [retries 5]
        (let [response @(http-client/get "http://localhost/status")
              body (json/read-str (:body response) {:key-fn keyword})]
          (is (= 200 (:status response)))
          (is (not (nil? (:found-match-count body))))
          (is (not (nil? (:last-run body))))
          (is (not (nil? (:new-match-count body))))
          (is (not (nil? (:parsed-match-count body))))
          (is (not (nil? (:valid-match-count body))))
          (is (= (:valid-matches body)
                 [{:date       "2023-09-05"
                   :home-team  "Flying Circus"
                   :guest-team "Kickertrupp (NR)"
                   :link       "https://kickern-hamburg.de//liga/ergebnisse-und-tabellen?task=begegnung_spielplan&veranstaltungid=229&id=15012"}])))
        (when (> retries 0)
          (Thread/sleep 50)
          (recur (- retries 1))))
      (catch Exception e
        (println "test failed" e))
      (finally (server/stop-server server)))))
