(ns kicker-league-scanner.http-server-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [kicker-league-scanner.http-server :as server]
            [org.httpkit.client :as http-client]))

(defn scheduled-fn-mock [app-status] (reset! app-status {:found-matches  1
                                                         :new-matches    1
                                                         :parsed-matches 1
                                                         :valid-matches  1
                                                         :last-run       0}))
(deftest create-status-handler-test
  (let [server (server/start-server {:scheduled-fn       scheduled-fn-mock
                                     :scheduled-interval 5000})]
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
  (let [server (server/start-server {:scheduled-fn       scheduled-fn-mock
                                     :scheduled-interval 1})]
    (try
      (loop [retries 5]
        (let [response @(http-client/get "http://localhost/status")
              body (json/read-str (:body response))]
          (is (= 200 (:status response)))
          (is (not (nil? (get body "found-matches"))))
          (is (not (nil? (get body "last-run"))))
          (is (not (nil? (get body "new-matches"))))
          (is (not (nil? (get body "parsed-matches"))))
          (is (not (nil? (get body "valid-matches")))))
        (when (> retries 0)
          (Thread/sleep 50)
          (recur (- retries 1))))
      (catch Exception e
        (println "test failed" e))
      (finally (server/stop-server server)))))
