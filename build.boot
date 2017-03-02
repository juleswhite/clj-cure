(set-env!
 :source-paths #{"src"}
 :dependencies
   '[[org.clojure/clojure "1.8.0"] 
     [loco "0.3.1"]
     [org.clojure/tools.cli "0.3.5"]
     [org.clojure/tools.reader "1.0.0-beta4"]]])

(task-options!
  pom {:project 'clj-cure
       :version "0.1.0"}
  jar {:manifest {"Manifest-Version" "1.0"
                  "Main-Class" "cure.main"}})

(deftask build
  "Build my project and put it in the local repository."
  []
  (comp (pom) (jar) (install)))
  
