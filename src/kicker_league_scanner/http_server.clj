(ns kicker-league-scanner.http-server
  (:require [clojure.core.async :as async]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [kicker-league-scanner.io :as io]
            [org.httpkit.server :as hk-server]
            [overtone.at-at :as at]))

(defn create-status-handler [app-status request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str @app-status)})

(defn not-found-response [_]
  {:status  404
   :headers {"Content-Type" "text/plain"}
   :body    "Not Found"})

(defn routes [app-status]
  (fn [request]
    (case (:uri request)
      "/status" (create-status-handler app-status request)
      (not-found-response request))))

(defn stop-server [server-handle]
  (hk-server/server-stop! server-handle))

(def twenty-four-hours 86400000)

(defn start-server [{:keys [scheduled-fn scheduled-interval] :as options}]
  (let [app-status (atom {:found-match-count  nil
                          :new-match-count    nil
                          :parsed-match-count nil
                          :valid-match-count  nil
                          :valid-matches      nil
                          :last-run           nil})
        at-pool (at/mk-pool)
        scheduled-job (if (and scheduled-fn scheduled-interval)
                        (at/every scheduled-interval (partial scheduled-fn app-status) at-pool {:initial-delay scheduled-interval})
                        (at/every twenty-four-hours (partial io/update-data! options app-status) at-pool))
        http-server (hk-server/run-server (routes app-status) {:port                 80
                                                               :legacy-return-value? false})]
    (log/info (str "http server started: http://localhost"))
    http-server))

(defn start-server-blocking-mode [options]
  (start-server options)
  (async/<!! (async/chan)))