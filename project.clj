(defproject clj-cure "0.1.0-SNAPSHOT"
  :description "Constraint UR Environment"
  :url "https://github.com/juleswhite/clj-cure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-midje "3.0.0"] 
            [venantius/ultra "0.5.1"] 
            [com.jakemccrary/lein-test-refresh "0.18.1"]]
  :dependencies [[org.clojure/clojure "1.8.0"] 
                 [loco "0.3.1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.reader "1.0.0-beta4"]]
  :main ^:skip-aot cure.main
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
