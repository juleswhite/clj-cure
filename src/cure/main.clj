(ns cure.main
    (:require 
        [clojure.tools.cli :as cli]
        [clojure.java.io :as io]
        [cure.core :as core]
        [clojure.string :as string]
        [clojure.pprint :as pp]
        [clojure.tools.reader.edn :as edn])
    (:gen-class))

(def cli-options
  ;; An option with a required argument
  [["-i" "--input FILE" "file path"
    :default "./input.cure"
    ;; :parse-fn #()
    :validate [#(.exists (io/file %)) "Must be a valid file path"]]
   ;; A non-idempotent option
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Feature planning."
        ""
        "Usage: cure [options]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "Need some type of command line interface to pass 
   in configuration definitions in Clojure"
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
     (cond
         (:help options) (exit 0 (usage summary))
         errors (exit 1 (error-msg errors)))
     (pp/pprint options)
     (with-open [input (->> (:input options)
                          clojure.java.io/reader
                          java.io.PushbackReader.)]
        (let [fm (repeatedly (partial edn/read {:eof :theend} input))]
           (try
              (pp/pprint (concat (take-while (partial not= :theend) fm)))
              (catch Exception ex 
                  (pp/pprint (.getMessage ex))))))))
