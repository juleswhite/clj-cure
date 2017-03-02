
(set-env!
 :resource-paths #{"res"}
 :source-paths #{"src"}
 :dependencies
   '[[org.clojure/clojure "1.8.0"] 
     [loco "0.3.1"]
     [org.clojure/tools.cli "0.3.5"]
     [org.clojure/tools.reader "1.0.0-beta4"]
     [adzerk/bootlaces "0.1.13" :scope "test"]])

(require '[boot.git :refer [last-commit]]
         '[adzerk.bootlaces :as laces])
(def +version+ "0.1.0")
(laces/bootlaces! +version+)

(task-options!
  repl {:init-ns 'cure.main}
  push {:repo           "deploy"
        :ensure-branch  "master"
        :ensure-clean   true
        :ensure-tag     (last-commit)
        :ensure-version +version+}
  pom {:project 'juleswhite/clj-cure
       :version +version+
       :description "Constraint UR Environment"
       :packaging "jar"
       :url "https://github.com/juleswhite/clj-cure"
       :scm {:url "https://github.com/juleswhite/clj-cure"
             :connection "scm:git:git://github.com/juleswhite/clj-cure.git"
             :developerConnection "scm:git:ssh://git@github.com/juleswhite/clj-cure.git"
             :tag ""}
       :license
          {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}
       :developers {"Jules White" ""
                    "Fred Eisele" "fredrick.eisele@gmail.com"}}
  jar {:manifest {"Manifest-Version" "1.0"
                  "Built-By" (System/getProperty "user.name")
                  "Created-By" (format "Boot %s" boot.core/*boot-version*)
                  "Build-Jdk" (System/getProperty "java.specification.version")
                  "Main-Class" "cure.main"}
       :main 'cure.main})

(deftask build
  "Build my project and put it in the local repository."
  []
  (laces/build-jar))
  
