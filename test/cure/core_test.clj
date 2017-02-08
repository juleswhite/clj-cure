(ns cure.core-test
  (:require [clojure.test :refer :all]
            [cure.core :refer :all])
  )

(deftest unbounded-constraints-test
  (testing "a feature model with no constraints or goals"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)])
           solutions (all-configurations model)]
      (is (= 32 (count solutions))))))


(deftest pure-logical-constraints-test
  (testing "a feature model with only logical constraints"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (requires :a 2 3 [:b :c :d])
                    (requires :e 1 1 [:b :d])
                    (excludes :e [:c])
                    (requires :b 1 1 [:e])
                    (excludes :b [:d])
                    (requires :c 1 1 [:d])
                    (selected [:a :c])])
           solution (configuration model)]
      (is (has solution :a))
      (is (has solution :c))
      (is (has solution :d))
      (is (not (has solution :e))))))


(deftest pure-logical-constraints-contradictory
  (testing "a feature model with only logical constraints that contradict each other"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (requires :a 2 3 [:b :c :d])
                    (requires :e 1 1 [:b :d])
                    (excludes :e [:c])
                    (requires :b 1 1 [:e])
                    (excludes :b [:d])
                    (excludes :d [:c])
                    (requires :c 1 1 [:d])
                    (selected [:a :c])])
           solutions (all-configurations model)]
      (is (= (count solutions) 0)))))


(deftest pure-logical-constraints-contradictory-goals
  (testing "a feature model with only logical constraints and goals (e.g. selected/deslected) that contradict each other"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (requires :a 2 3 [:b :c :d])
                    (requires :e 1 1 [:b :d])
                    (excludes :e [:c])
                    (requires :b 1 1 [:e])
                    (excludes :b [:d])
                    (requires :c 1 1 [:d])
                    (selected [:a :c])
                    (deselected [:a :c])])
           solutions (all-configurations model)]
      (is (= (count solutions) 0)))))


(deftest mixed-logical-and-resource-constraints-test
  (testing "a mixed feature model with a single resource constraint"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (requires :e 1 1 [:b :d])
                    (excludes :b [:d])
                    (requires :a 2 3 [:b :c :d])
                    (resource_limit :cpu 18 {:a 10 :b 4 :e 1})
                    (selected [:a])])
           solution (configuration model)]
      (is (has solution :a))
      (is (or (has solution :b) (has solution :c)))
      (is (or (has solution :c) (has solution :d)))
      (is (>= (+ (get solution :b) (get solution :c) (get solution :d)) 2))
      (is (not (and (has solution :b) (has solution :d))))
      (is (<= (get solution :cpu) 18)))))


(deftest pure-resource-constraints-test-viable
  (testing "a feature model with only resource constraints and a viable solution"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (resource_limit :cpu 21 {:a 10 :b 4 :e 1})
                    (selected [:a :c])])
           solution (configuration model)]
      (is (has solution :a :c))
      (is (<= (get solution :cpu) 21)))))


(deftest pure-resource-constraints-test-with-exactly-one-solution
  (testing "a feature model with both logical and resource constraints and exactly one viable solution"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (requires :a 2 3 [:b :c :d])
                    (requires :e 1 1 [:b :d])
                    (excludes :e [:c])
                    (resource_limit :cpu 11 {:a 10 :b 4 :e 1})
                    (selected [:a :c])])
           solution (configuration model)]
      (is (has solution :a :c :d))
      (is (not (has solution :b :e)))
      (is (<= (get solution :cpu) 11)))))


(deftest pure-resource-constraints-of-multiple-types
  (testing "a feature model with multiple types of resource constraints and no logical constraints"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (resource_limit :cpu 11 {:a 10 :b 4 :e 1})
                    (resource_limit :memory 8 {:a 6 :b 4 :c 1})
                    (resource_limit :network 3 {:a 2 :b 4 :e 1})
                    (selected [:a :c])])
           solution (configuration model)]
      (is (has solution :a :c))
      (is (not (has solution :b :e)))
      (is (<= (get solution :cpu) 11))
      (is (<= (get solution :memory) 8))
      (is (<= (get solution :network) 3)))))


(deftest pure-resource-constraints-test-non-viable
  (testing "a feature model with only resource constraints and no viable solution"
    (let [ model (feature-model [
                    (feature :a)
                    (feature :b)
                    (feature :c)
                    (feature :d)
                    (feature :e)
                    (resource_limit :cpu 9 {:a 10 :b 4 :e 1})
                    (selected [:a :c])])
           solution (configuration model)]
      (is (not (has solution :a)))
      (is (not (has solution :b)))
      (is (not (has solution :c)))
      (is (not (has solution :d)))
      (is (not (has solution :e))))))
