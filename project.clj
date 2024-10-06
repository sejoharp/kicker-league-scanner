(defproject kicker-league-scanner "1.0.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clj-commons/hickory "0.7.4"]
                 [clojure.java-time "1.4.2"]
                 [clj-http "3.12.3"]
                 [cli-matic "0.5.4"]
                 [org.clojure/data.csv "1.1.0"]
                 [net.clojars.aneilm/lein-project-reader "0.0.1"]
                 [org.apache.commons/commons-compress "1.26.2"]]

  :main ^:skip-aot kicker-league-scanner.core
  :repl-options {:init-ns de.otto.jarvis.zerometers.core}

  :jvm-opts ["-Xverify:none"]

  :source-paths ["src"]
  :resource-paths ["resources"]
  :test-paths ["test"]

  :test-selectors {:default (constantly true)
                   :unit    :unit
                   :focused :focused
                   :all     (constantly true)}

  :aliases {"tr"   ["test-refresh" ":focused"]
            "trf"  ["test-refresh" ":focused"]
            "test" ["do" ["nsorg" "--replace"] ["cljfmt" "fix"] "test"]}

  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies []
                       :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                      [lein-ancient "0.6.14"]
                                      [lein-nvd "0.5.4"]
                                      [lein-nsorg "0.2.0"]
                                      [lein-cljfmt "0.6.4"]]
                       :injections   []}
             :test    {:jvm-opts       [~(str "-Djava.io.tmpdir=" (System/getenv "HOME"))]
                       :resource-paths ["test-resources"]
                       :injections     []}}
  )
