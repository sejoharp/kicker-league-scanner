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

(defn completed-match? [[result-info & remaining]]
  (let [first-character (first result-info)]
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
                                  "begegnung_spielplan")))
         (map #(get-in % [:attrs :href])))))

(defn find-game-snippets [match-page]
  (s/select (s/descendant (s/or (s/class "sectiontableentry1")
                                (s/class "sectiontableentry2")))
            match-page))
(defn parse-game [parsed-html]
  (let [body (:content parsed-html)
        position (->> body
                      second
                      :content
                      first
                      Integer/parseInt)
        first-home-player (->> body
                               (#(nth % 5))
                               :content
                               second
                               :content
                               first)
        first-guest-player (->> body
                                (#(nth % 9))
                                :content
                                second
                                :content
                                first)
        scores (->> body
                    (#(nth % 7))
                    :content
                    first
                    (#(str/split % #":"))
                    (map #(Integer/parseInt %)))]

    {:home     {:names [first-home-player]
                :score (first scores)}
     :guest    {:names [first-guest-player]
                :score (second scores)}
     :position position}))
(defn get-games-from-match [parsed-html]
  (let [link-maps (s/select (s/descendant (s/or (s/class "sectiontableentry1")
                                                (s/class "sectiontableentry2")))
                            parsed-html)]
    #_(->> link-maps
           (filter #(and
                     (completed-match? (:content %))
                     (some? (get-in % [:attrs :href]))
                     (str/includes? (get-in % [:attrs :href])
                                    "begegnung_spielplan")))
           (map #(get-in % [:attrs :href])))
    "not implemented yet"))

(defn -main []
  (println "Hello, World!"))

