(defproject cure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-midje "3.0.0"] [venantius/ultra "0.5.1"] [com.jakemccrary/lein-test-refresh "0.18.1"]]
  :dependencies [[org.clojure/clojure "1.8.0"] [loco "0.3.1"]]
  :main ^:skip-aot cure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
