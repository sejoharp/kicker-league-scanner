(ns kicker-league-scanner.core
  (:require [clojure.string :as str]
            [hickory.core :as h]
            [hickory.select :as s])

  (:gen-class))
(defn parse-links [parsed-html]
  (let [html (slurp "test/resources/league-overview.html")
        parsed-html (h/as-hickory (h/parse html))
        links (s/select [{:tag :a}] parsed-html)]
    (for [link links
          :let [text (get-in link [:content 0 :content])
                href (get-in link [:attrs :href])]
          :when (= text "Begegnungen …")]
      href)))

(defn read-league-overview [overview-link]
  (let [html (slurp overview-link)]
    (h/as-hickory (h/parse html))))

(defn parse-game-links [parsed-html]
  (let [link-htmls (s/select (s/descendant (s/class "readon")) parsed-html)
        begegnungen-links (filter #(str/includes? (:content %) "Begegnungen …") link-htmls)
        links (map #(get-in % [:attrs :href]) begegnungen-links)]
    links))

(defn -main []
  (println "Hello, World!"))

