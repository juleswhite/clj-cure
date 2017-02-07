(ns cure.core-test
  (:require [clojure.test :refer :all]
            [cure.core :refer :all]))



(deftest a-test
  (testing "FIXME, I fail."
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
      (is (>= (+ (get solution :b) (get solution :c) (get solution :d)) 2)))))
