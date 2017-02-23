(ns cure.core
 ;(:gen-class)
)

(require '[loco.core :as solver] '[loco.constraints :as con])

;; Need some type of command line interface to pass in configuration
;; definitions in Clojure
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


;; Creates a boolean variable indicating the presence or
;; absence of a feature in a configuration
(defn feature [name]
  {:fm-constraint {:type :declare-feature :features feature}
   :realization (con/$in name 0 1)})


;; Uses the built-in knapsack constraint to define a resource
;; constraint governing the slection of features
(defn resource_limit [rname amount consumers]
  (let [ consumer_pairs (seq consumers)
         occurence_vars (map #(first %) consumer_pairs)
         vname (keyword (str "_" (name rname) ".value"))
         value_vars (take (count consumers) (repeat 1))
         weight_vars (map #(second %) consumer_pairs)]
    {:fm-constraint {:type :resource-limit :resource rname :amount amount :consumption-map consumers}
     :realization
        (list
          (con/$in rname 0 amount)
          (con/$in vname 0 (count consumer_pairs))
          (con/$knapsack weight_vars value_vars occurence_vars rname vname)
          (con/$<= rname amount))}))

;; Specifies that a feature requires between min..max of the target features or exactly count of them
(defn requires
  ([parent min max children]
     {:fm-constraint {:type :requires :parent parent :features children :min min :max max}
       :realization
       (con/$if (con/$= parent 1)
                (con/$and
                  (con/$<= (apply con/$+ children) max)
                  (con/$>= (apply con/$+ children) min)))})
  ([parent count children]
     {:fm-constraint {:type :requires :parent parent :min count :max count :features children}
       :realization
       (con/$if (con/$= parent 1) (con/$= (apply con/$+ children) max))}))

;; Specifies that the provided parent is mutually exclusive with all features specified in children
(defn excludes [parent children]
  {:fm-constraint {:type :excludes :parent parent :features children}
   :realization
    (con/$if (con/$= parent 1) (con/$= (apply con/$+ children) 0))})

;; Forces the selection of the given features
(defn selected [features]
  {:fm-constraint {:type :selected :features features}
   :realization (con/$= (apply con/$+ features) (count features))})

;; Prevents the selection of the given features
(defn deselected [features]
  {:fm-constraint {:type :selected :features features}
   :realization (con/$= (apply con/$+ features) 0)})

;; Provides a method for making a constraint relaxable where
;; the solver can choose not to enforce it. This is used to
;; support operations like "find the closest valid configuration"
;; to a set of unsatisfiable constraints given to the solver.
(defn relax-constraint [con id]
  (let [real-con (con :realization)]
    (if (= (real-con :type) :int-domain)
      {:relax-vars [] :constraints [real-con]}
      {:relax-vars [id]
         :constraints
          [{:realization [(con/$in id 0 1)
           (con/$or real-con (con/$= id 1))]}]})))

;; Allows a user to express constraints that can be relaxed
;; if necessary to find a viable solution. The first argument
;; is the original feature model constraints and any requirements,
;; such as selected features. The second argument is a list of the
;; constraints that can be violated if necessary.
;;
;;
;; Example where we force the solver to find a solution that requires
;; making changes to our constraints using the list of relaxable constraints.
;;
;; (configuration
;;  (feature-model
;;    (relax-constraints
;;      [(feature :a)
;;      (feature :b)
;;      (feature :c)
;;      (feature :d)
;;      (feature :e)
;;      (requires :a 2 2 [:b :d])
;;      (excludes :b [:d])
;;      (resource_limit :cpu 12 {:a 10 :b 4 :e 1})
;;      (selected [:a])]
;;      [(requires :a 2 2 [:b :d]) (excludes :b [:d])])))
(defn relax-constraints [fm relaxation-rules]
  (let [relaxation-set (into #{} (map #(% :fm-constraint) relaxation-rules))
        unaffected (filter #(not (contains? relaxation-set (% :fm-constraint))) fm)
        affected (filter #(contains? relaxation-set (% :fm-constraint)) fm)
        relaxed (map-indexed (fn [index con] (relax-constraint con (keyword (str "relaxed_" index)))) affected)
        relaxvars (flatten (map #(% :relax-vars) relaxed))
        relaxsum {:realization [(con/$in :changes 0 (count relaxvars)) (con/$= (apply con/$+ relaxvars) :changes )]}]
      (flatten (concat (flatten (concat unaffected (map #(% :constraints) relaxed))) [relaxsum]))))

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
  (apply solver/solutions (args model options)))

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
  (apply solver/solution (args model options)))

;; Constructs a feature model with the given constraints
(defn feature-model [constraints]
  (vec (flatten (map #(% :realization) constraints))))


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
;; (configuration tm :maximize (con/$+ :a :b :d))
;;
;; Get all configurations
;;
;; (all-configurations tm)
;;
;; Limit how long we configure:
;;
;; (all-configurations tm :timeout 5000)

