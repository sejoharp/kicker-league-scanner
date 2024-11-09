(ns kicker-league-scanner.csv-format
  (:require [clojure.string :as str]))

(defn calculate-game-points [game] (cond
                                     (= 6 (:score (:home game))) [2 0]
                                     (= 6 (:score (:guest game))) [0 2]
                                     (= (:score (:home game)) (:score (:guest game))) [1 1]
                                     :else [0 0]))

(defn calculate-match-score [games]
  (let [game-points (map calculate-game-points games)]
    (loop [game-points-list game-points
           result [0 0]]
      (if (= 0 (count game-points-list))
        result
        (let [home-points (first (first game-points-list))
              guest-points (second (first game-points-list))]
          (recur (rest game-points-list) [(+ (first result)
                                             home-points)
                                          (+ (second result)
                                             guest-points)]))))))

(defn calculate-points [match]
  (let [match-scores (calculate-match-score (:games match))]
    (cond
      (= (first match-scores) (second match-scores)) [1 1]
      (> (first match-scores) (second match-scores)) [2 0]
      (< (first match-scores) (second match-scores)) [0 2])))

(defn calculate-quarter [date]
  (let [month (Integer/parseInt (second (str/split date #"-")))
        year (first (str/split date #"-"))]
    (cond
      (<= month 3) (str year "/01")
      (<= month 6) (str year "/02")
      (<= month 9) (str year "/03")
      (>= month 10) (str year "/04"))))

(defn game->csv [match game]
  (let [home-players (:names (:home game))
        guest-players (:names (:guest game))
        game-points (calculate-game-points game)
        quarter (calculate-quarter (:date match))]
    [[(:date match)
      (:match-day match)
      (:position game)
      "H"
      (:home-team match)
      (first home-players)
      (if (= 2 (count home-players))
        (second home-players)
        "XXXX")
      (:score (:home game))
      (:score (:guest game))
      (first guest-players)
      (if (= 2 (count guest-players))
        (second guest-players)
        "XXXX")
      (:guest-team match)
      "G"
      (first game-points)
      (second game-points)
      quarter
      1]
     [(:date match)
      (:match-day match)
      (:position game)
      "H"
      (:home-team match)
      (if (= 2 (count home-players))
        (second home-players)
        "XXXX")
      (first home-players)
      (:score (:home game))
      (:score (:guest game))
      (if (= 2 (count guest-players))
        (second guest-players)
        "XXXX")
      (first guest-players)
      (:guest-team match)
      "G"
      (first game-points)
      (second game-points)
      quarter
      1]
     [(:date match)
      (:match-day match)
      (:position game)
      "G"
      (:guest-team match)
      (first guest-players)
      (if (= 2 (count guest-players))
        (second guest-players)
        "XXXX")
      (:score (:guest game))
      (:score (:home game))
      (first home-players)
      (if (= 2 (count home-players))
        (second home-players)
        "XXXX")
      (:home-team match)
      "H"
      (second game-points)
      (first game-points)
      quarter
      1]
     [(:date match)
      (:match-day match)
      (:position game)
      "G"
      (:guest-team match)
      (if (= 2 (count guest-players))
        (second guest-players)
        "XXXX")
      (first guest-players)
      (:score (:guest game))
      (:score (:home game))
      (if (= 2 (count home-players))
        (second home-players)
        "XXXX")
      (first home-players)
      (:home-team match)
      "H"
      (second game-points)
      (first game-points)
      quarter
      1]]))

(defn match->csv [{games :games :as match}]
  (let [game->csv-fn (partial game->csv match)]
    (reduce (fn [acc game]
              (into acc (game->csv-fn game)))
            []
            games)))