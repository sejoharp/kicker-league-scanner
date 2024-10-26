(ns kicker-league-scanner.http-server
  (:require [org.httpkit.server :as hk-server]))

(defn get-current-status [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "everything ist fine"})

(defn start-server []
  (hk-server/run-server get-current-status {:port 8080}))