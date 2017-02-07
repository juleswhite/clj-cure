
;;
;;
;;  Do not use unless there is a darn good reason!
;;
;;
;;  We use the resource_limit function in the core
;;  library instead of this manual version of resource
;;  limits. The resource_limit version delegates to the
;;  knapsack constraint of the underlying solver
;;

;; Creates a variable to hold the amount of the resource
;; consumed by the current selection state of a feature
(defn consumer_var_name [owner rname]
  (keyword (str "_" (name owner) "." (name rname))))

;; Creates a set of constraints to bind the selection of each
;; feature that consumes a resource to its corresponding
;; resource consumption variable for that resource
(defn consumers_constraint [rname amount consumers]
  (mapcat
        #(let [ consumer (first %)
                consumed (second %)
                vname (consumer_var_name consumer rname)]
          (list
             ($in vname 0 consumed)
             ($= ($* consumer consumed) vname)))
          (seq consumers)))

;; Returns a list of all the names to use for the consumer
;; variables for a given resource (e.g., :_a.cpu, :_b.cpu, etc..
(defn consumer_var_names [rname consumers]
    (vec (map
      #(consumer_var_name (first %) rname)
      (seq consumers))))

;; Adds a constraint limiting resource consumption for the consumers to
;; be less than the total quantity available for that resource
(defn consumed_constraint [rname amount consumers]
  (let [consumervars (consumer_var_names rname consumers)]
    (list ($in rname 0 amount) ($= (apply $+ consumervars) rname))))


;; Adds a constraint that models the consumption of a resource by features
;; and prevents the consumption from exceeding the specified value. Resources
;; are consumed when features are selected.
(defn resource [rname amount consumers]
  (vec (flatten
    (list
      (consumers_constraint rname amount consumers)
      (consumed_constraint rname amount consumers)))))
