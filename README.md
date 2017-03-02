# cure

Evolving feature model configurations in software product lines

http://www.sciencedirect.com/science/article/pii/S0164121213002434

Highlights
* We provide a formal model of multi-step configuration.
* We show how the formal model of multi-step configuration can be mapped to a CSP.
* We show how multi-step requirements can be specified using our CSP formulation of multi-step configuration.
* We present methods for modeling feature model drift as a feature model changes over time.
* We describe mechanisms for optimally deriving a set of configurations that meet the requirements and minimize or maximize a property (such as total configuration cost) of the configurations or configuration process.


## Installation

     git clone https://github.com/juleswhite/clj-cure.git
     cd clj-cure
     boot build

## Usage

FIXME: explanation

    $ java -jar cure-0.1.0-standalone.jar [args]

## Options

[//]: # (The options documentation is produced by running 'java -jar cure-0.1.0-standalone.jar -h')


## Examples

    (def tm (feature-model 
             [(feature :a)
              (feature :b)
              (feature :c)
              (feature :d)
              (feature :e)
              (requires :e 1 1 [:b :d])
              (excludes :b [:d])
              (requires :a 2 3 [:b :c :d])
              (resource_limit :cpu 12 {:a 10 :b 4 :e 1})
              (selected [:a])])]
  
    ;; Get a single solution:
    (configuration tm)
  
    ;; Minimize cpu:
    (configuration tm :minimize :cpu)
  
    ;; Select as many of a, b, d as possible:
    (configuration tm :maximize (con/$+ :a :b :d))
  
    ;; Get all configurations
    (all-configurations tm)
  
    ;; Limit how long we configure:
    (all-configurations tm :timeout 5000)))



### Bugs


## License

Copyright © 2017 Jules White

Distributed under the Eclipse Public License either version 1.0 
or (at your option) any later version.
