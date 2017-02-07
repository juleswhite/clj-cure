(ns cure.core
  (:gen-class))

(use 'loco.core 'loco.constraints)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


;; Creates a boolean variable indicating the presence or
;; absence of a feature in a configuration
(defn feature [name]
  ($in name 0 1))

;; Begining of an attempt to use the built-in knapsack cosntraint
(defn resource_limit [rname amount consumers]
  (let [ consumer_pairs (seq consumers)
         occurence_vars (map #(first %) consumer_pairs)
         vname (keyword (str "_" (name rname) ".value"))
         value_vars (take (count consumers) (repeat 1))
         weight_vars (map #(second %) consumer_pairs)]
    (list
      ($in rname 0 amount)
      ($in vname 0 (count consumer_pairs))
      ($knapsack weight_vars value_vars occurence_vars rname vname)
      ($<= rname amount))))

;; Specifies that a feature requires between min..max of the target features or exactly count of them
(defn requires
  ([parent min max children] ($if ($= parent 1) ($and ($<= (apply $+ children) max) ($>= (apply $+ children) min))))
  ([parent count children] ($if ($= parent 1) ($= (apply $+ children) max))))

;; Specifies that the provided parent is mutually exclusive with all features specified in children
(defn excludes [parent children]
  ($if ($= parent 1) ($= (apply $+ children) 0)))

;; Forces the selection of the given features
(defn selected [features]
  ($= (apply $+ features) (count features)))

;; Prevents the selection of the given features
(defn deselected [features]
  ($= (apply $+ features) 0))

;; Returns true if all of the specified features are in the
;; configuration (E.g., solution) previously returned
(defn has [configuration & features]
  (= (reduce + (map #(get configuration %) features)) (count features)))

(defn args [base extras]
  (remove nil? (concat [base] extras)))

;; Returns all possible valid configurations
;;
;; You may also pass in additional parameters for the solver:
;;
;; :maximize goal -- goal is feature identifier or a set of constraints over a
;;                   set of variables in the CSP (see: https://github.com/aengelberg/loco)
;; :minimize goal -- goal is feature identifier or a set of constraints over a
;;                   set of variables in the CSP (see: https://github.com/aengelberg/loco)
;; :timeout millis -- maximum time to look for a solution (see: https://github.com/aengelberg/loco)
;;
(defn all-configurations [model & options]
  (apply solutions (args model options)))

;; Returns a singlve valid configuration
;;
;; You may also pass in additional parameters for the solver:
;;
;; :maximize goal -- goal is feature identifier or a set of constraints over a
;;                   set of variables in the CSP (see: https://github.com/aengelberg/loco)
;; :minimize goal -- goal is feature identifier or a set of constraints over a
;;                   set of variables in the CSP (see: https://github.com/aengelberg/loco)
;; :timeout millis -- maximum time to look for a solution (see: https://github.com/aengelberg/loco)
;;
(defn configuration [model & options]
  (apply solution (args model options)))

;; Constructs a feature model with the given constraints
(defn feature-model [constraints]
  (vec (flatten constraints)))


;; Example feature selection problem
;;
;;(def tm (feature-model [
;;  (feature :a)
;;  (feature :b)
;;  (feature :c)
;;  (feature :d)
;;  (feature :e)
;;  (requires :e 1 1 [:b :d])
;;  (excludes :b [:d])
;;  (requires :a 2 3 [:b :c :d])
;;  (resource_limit :cpu 12 {:a 10 :b 4 :e 1})
;;  (selected [:a])]))
;;
;; Get a single solution:
;;
;; (configuration tm)
;;
;; Minimize cpu:
;;
;; (configuration tm :minimize :cpu)
;;
;; Select as many of a, b, d as possible:
;;
;; (configuration tm :maximize ($+ :a :b :d))
;;
;; Get all configurations
;;
;; (all-configurations tm)
;;
;; Limit how long we configure:
;;
;; (all-configurations tm :timeout 5000)

