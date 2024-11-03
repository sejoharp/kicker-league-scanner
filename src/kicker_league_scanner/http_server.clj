(ns kicker-league-scanner.http-server
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as hk-server]
            [clojure.data.json :as json]
            [clojure.core.async :as async]))

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

(defn start-server
  ([options]
   (start-server (async/chan) options))
  ([close-channel options]
   (let [app-status (atom {:found-matches  nil
                           :new-matches    nil
                           :parsed-matches nil
                           :valid-matches  nil
                           :last-run       nil})
         http-server (hk-server/run-server (create-app app-status) {:port 80})]
     (log/info (str "http server started: http://localhost"))
     (when (async/<!! close-channel)
       (http-server)))))
