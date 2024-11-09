(ns kicker-league-scanner.core
  (:require [cli-matic.core :as cli-matic]
            [kicker-league-scanner.cli :as cli]
            [kicker-league-scanner.io :as io])
  (:gen-class))




;TODO: change author
;  howto: https://gist.github.com/amalmurali47/77e8dc1f27c791729518701d2dec3680

;TODO: add closable system:
;  https://gist.github.com/andfadeev/176abae0a0d55b90492c67d2978ba6c0
;  https://www.youtube.com/watch?v=a1TvDcDop2k
;  https://medium.com/@maciekszajna/reloaded-workflow-out-of-the-box-be6b5f38ea98

;TODO: expose state with timestamp via status page to monitor with updatekuma

;TODO: deploy to lxc with alpine linux and create a daemon with OpenRC

;TODO: build jar with github actions


(defn -main [& args]
  (cli-matic/run-cmd args (cli/create-cli-config)))

