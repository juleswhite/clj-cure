(ns cure.digitaltwin
    (:require
        [clojure.pprint :refer [print-table]]
        [loco.core :refer :all] 
        [loco.constraints :refer :all]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;; 
;; To run this example, you will need to install:
;;
;; 1. JDK 8+
;; 2. Leiningen https://leiningen.org/
;;
;; To run the example:
;;
;; 1. lein repl
;; 2. At the repl prompt: (require '[cure.digitaltwin :as twin :refresh true])
;; 3. At the repl prompt: (twin/run-examples)
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn run-examples []
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; Assume that we have an engine where we expect the number of 
  ;; engine "rebuilds" should be given by the formula:
  ;;
  ;; rebuilds = flights * durability_coeff + dusty_flights * dusty_durability_coeff
  ;;
  ;; The manufacturer states that the total number of engine rebuilds, regardless
  ;; of conditions, should be between 1 and 32 for 100 flights 
  ;;
  ;; The manufacturer has provided the expected durability coefficients as the
  ;; number of rebuilds per 100 flights:
  ;;
  ;; durability_coeff = 15
  ;; dusty_durability_coeff = 32
  ;;
  ;; The flights and dusty_flights are rounded to the nearest 100 flights
  ;; and divided by 100 before being entered into the model.  
  ;;
  ;; We observe how many rebuilds happen over a set of flights and thn
  ;; compare the number of real-world rebuilds to what is modeled. We
  ;; want the real-world rebuilds to be better than or equal to what is
  ;; predicted by the digital twin.
  ;;
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (println "=========================================================")
  (println "Solutions to initial digital twin vs. actual observed data")
  (println "=========================================================")
  (print-table
   (solutions
      ;; Model from manufacturer / digital twin
     [($in :actual_rebuilds 0 100)
      ($in :predicted_rebuilds 1 32)  ; rebuilds is in the domain ranging from 1 to 10, inclusive
      ($in :durability_coeff 1 100)  ; durability_coeff is in the domain ranging from 1 to 10, inclusive
      ($in :dusty_durability_coeff 1 100)
      ($= :durability_coeff 15)
      ($= :dusty_durability_coeff 32)
      ($= :predicted_rebuilds
          ($+ 
             ($* :regular_flights :durability_coeff)
             ($* :dusty_flights   :dusty_durability_coeff)))

      ;; Actual recorded data from real-world
      ($in :regular_flights 0 100) ; normalized to 1 = 100 flights, 2 = 200 flights, etc...
      ($in :dusty_flights 0 100)   ; normalized to 1 = 100 flights, 2 = 200 flights, etc...
      ($= :regular_flights 1)
      ($= :dusty_flights 0)
      ($= :actual_rebuilds 14)

      ;; We expect actual performance to be equal or better than the digital twin
      ($<= :actual_rebuilds :predicted_rebuilds)]))


  (println "\n\n\n\n")


  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; Assume everything remains the same, except that the 
  ;; manufacturer states that the total number of engine rebuilds, regardless
  ;; of conditions, should be between 1 and 10 for 100 flights.
  ;;
  ;; We observe the same real-world operation, but now the model has a constraint
  ;; violation and the solver can't find a solution that works.
  ;; 
  ;; There are a couple of issues:
  ;;
  ;; 1. The digital twin model for the maximum number of rebuilds, regardless
  ;;    of condition is wrong, since more rebuilds are possible if there are
  ;;    a lot of dusty flights
  ;; 2. The digital twin model for calculating rebuilds as a function of regular
  ;;    and dusty flights is wrong
  ;; 3. The actual observed rebuilds is 14, which is higher than the maixmum 
  ;;    predicted number of 10
  ;;
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (println "=========================================================")
  (println "Solutions to initial digital twin vs. actual observed data")
  (println "=========================================================")
  (let [sols
        (solutions
           ;; Model from manufacturer / digital twin
          [($in :actual_rebuilds 0 100)
           ($in :predicted_rebuilds 1 10)  ; rebuilds is in the domain ranging from 1 to 10, inclusive
           ($in :durability_coeff 1 100)  ; durability_coeff is in the domain ranging from 1 to 10, inclusive
           ($in :dusty_durability_coeff 1 100)
           ($= :durability_coeff 15)
           ($= :dusty_durability_coeff 32)
           ($= :predicted_rebuilds
               ($+ 
                  ($* :regular_flights :durability_coeff)
                  ($* :dusty_flights   :dusty_durability_coeff)))

           ;; Actual recorded data from real-world
           ($in :regular_flights 0 100) ; normalized to 1 = 100 flights, 2 = 200 flights, etc...
           ($in :dusty_flights 0 100)   ; normalized to 1 = 100 flights, 2 = 200 flights, etc...
           ($= :regular_flights 1)
           ($= :dusty_flights 0)
           ($= :actual_rebuilds 14)

           ;; We expect actual performance to be equal or better than the digital twin
           ($<= :actual_rebuilds :predicted_rebuilds)])] ;; ==> No solution
   (if (not-empty sols)
       (print-table sols)
       (println "No solutions.")))


  (println "\n\n\n\n")
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; Assume everything remains the same, except that the 
  ;; manufacturer states that the total number of engine rebuilds, regardless
  ;; of conditions, should be between 1 and 10 for 100 flights.
  ;;
  ;; We observe the same real-world operation, but now the model has a constraint
  ;; violation and the solver can't find a solution that works.
  ;; 
  ;; There are a couple of issues:
  ;;
  ;; 1. The digital twin model for the maximum number of rebuilds, regardless
  ;;    of condition is wrong, since more rebuilds are possible if there are
  ;;    a lot of dusty flights
  ;; 2. The digital twin model for calculating rebuilds as a function of regular
  ;;    and dusty flights is wrong
  ;; 3. The actual observed rebuilds is 14, which is higher than the maixmum 
  ;;    predicted number of 10
  ;;
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (println "=========================================================")
  (println "Most likely error in the digital twin model:")
  (println "=========================================================")
  (print-table
   [
    (select-keys
     (first
      (solutions
         ;; Model from manufacturer / digital twin
        [($in :actual_rebuilds 0 100)
         ($in :predicted_rebuilds 1 100)  ; rebuilds is in the domain ranging from 1 to 10, inclusive
         ($in :durability_coeff 1 100)  ; durability_coeff is in the domain ranging from 1 to 10, inclusive
         ($in :dusty_durability_coeff 1 100)

         ;; Actual recorded data from real-world
         ($in :regular_flights 0 100) ; normalized to 1 = 100 flights, 2 = 200 flights, etc...
         ($in :dusty_flights 0 100)   ; normalized to 1 = 100 flights, 2 = 200 flights, etc...
         ($= :regular_flights 1)
         ($= :dusty_flights 0)
         ($= :actual_rebuilds 16)

         ;; More digital twin model constraints
         ($= :predicted_rebuilds
            ($+ 
               ($* :regular_flights :durability_coeff)
               ($* :dusty_flights   :dusty_durability_coeff)))

         ;; Possible digital twin errors
         ($in :durability_coeff_error 0 1)
         ($in :dusty_durability_coeff_error 0 1)
         ($in :predicted_rebuilds_max_error  0 1)


         ;; Diagnosis constraints
         ($or ($= :durability_coeff 15) ($= :durability_coeff_error 1))
         ($or ($= :dusty_durability_coeff 32) ($= :dusty_durability_coeff_error 1))


         ($or
           ($< :actual_rebuilds :predicted_rebuilds)
           ($= :predicted_rebuilds_max_error 1))

         ;; We expect actual performance to be equal or better than the digital twin
         ($<= :actual_rebuilds :predicted_rebuilds)] 

        :minimize ($+ :durability_coeff_error :dusty_durability_coeff_error :predicted_rebuilds_max_error)
        :timeout 1000))
     [:durability_coeff_error :dusty_durability_coeff_error :predicted_rebuilds_max_error])]))


(comment
  (run-examples))
