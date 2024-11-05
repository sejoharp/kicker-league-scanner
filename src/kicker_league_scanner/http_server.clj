(ns kicker-league-scanner.http-server
  (:require [clojure.core.async :as async]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [org.httpkit.server :as hk-server]))

(defn create-status-handler [app-status request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str @app-status)})

(defn not-found-response [_]
  {:status  404
   :headers {"Content-Type" "text/plain"}
   :body    "Not Found"})

(defn create-app [app-status]
  (fn [request]
    (case (:uri request)
      "/status" (create-status-handler app-status request)
      (not-found-response request))))

(defn stop-server [server-handle]
  (hk-server/server-stop! server-handle))

(defn start-server [options]
  (let [app-status (atom {:found-matches  nil
                          :new-matches    nil
                          :parsed-matches nil
                          :valid-matches  nil
                          :last-run       nil})
        http-server (hk-server/run-server (create-app app-status) {:port                 80
                                                                   :legacy-return-value? false})]
    (log/info (str "http server started: http://localhost"))
    http-server))

(defn start-server-blocking-mode [options]
  (start-server options)
  (async/<!! (async/chan)))