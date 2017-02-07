(ns cure.core
  (:gen-class))


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
    (has solution :a :c)
  )
