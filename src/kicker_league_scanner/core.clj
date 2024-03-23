(ns kicker-league-scanner.core
  (:require [clojure.string :as str]
            [hickory.core :as h]
            [hickory.select :as s])

  (:gen-class))

(defn html->hickory [overview-link]
  (let [html (slurp overview-link)]
    (h/as-hickory (h/parse html))))

(defn get-leagues [])

(defn get-league-links-from-league-overview [parsed-html]
  (let [link-htmls (s/select (s/descendant (s/class "readon"))
                             parsed-html)
        begegnungen-links (filter #(str/includes? (:content %) "Begegnungen …")
                                  link-htmls)
        links (map #(get-in % [:attrs :href])
                   begegnungen-links)]
    links))

(defn completed-match? [[actual-result & remaining]]
  (let [first-character (first actual-result)]
    (contains? (set "0123456789") first-character)))

(defn get-match-links-from-league [parsed-html]
  (let [link-maps (s/select (s/descendant (s/or (s/class "sectiontableentry1")
                                                (s/class "sectiontableentry2"))
                                          (s/tag :a))
                            parsed-html)]
    (->> link-maps
         (filter #(and
                    (completed-match? (:content %))
                    (some? (get-in % [:attrs :href]))
                    (str/includes? (get-in % [:attrs :href])
                                   "begegnung_spielplan")
                    ))
         (map #(get-in % [:attrs :href]))
         )))

(defn parse-games-from-match [parsed-html])

(defn -main []
  (println "Hello, World!"))

