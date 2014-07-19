(ns entrez-vous.console
  "A command-line interface for entrez-vous: scrape author abstracts from Pubmed."
  (:require [clojure.string  :as str]
            [clojure.java.io :as io])
  (:gen-class :main true)
  (:use [entrez-vous.core :only [retrieve-author-uids
                                 retrieve-related-uids
                                 retrieve-abstracts]]
        [clojure.tools.cli :only [cli]]))

(defn parse-args
  "Defines arguments for the program."
  [args]
  (cli args
       "This program scrapes author abstracts from NCBI Pubmed database."
       ["-h" "--help" "Prints the help banner." :default false :flag true]
       ["-d" "--date" "Append date-stamps." :default false :flag true]
       ["-s" "--split" "Split by articles." :default false :flag true]
       ["-n" "--name" "Add an author name to target."
        :assoc-fn (fn [previous key val]
                    (assoc previous key
                           (if-let [oldval (get previous key)]
                             (merge oldval val)
                             (hash-set val))))]
       ["-i" "--input" "Read author names from a text file (one name per line)."]
       ["-h" "--hop" "Hop to related papers." :default false :flag true]
       ["-l" "--limit" "Limit # of relevant papers." :default 1000 :parse-fn #(Integer. %)]
       ["-o" "--output" "Choose an output directory." :default "."]))

(defn clean-author-name
  "Process author name. Make lowercase and replace spaces with
  hyphens. If the author name is suceeded by any form of MD or PhD, it
  is removed."
  [author-name]
  (as-> author-name x
        (str/replace x #"(?i)m\.?d\.?|phd" " ")
        (str/trim x)
        (str/replace x " " "-")
        (str/lower-case x)))

(defn base-filename
  "Creates a directory structure based on the author's name and an optional suffix."
  ([output-directory author-name suffix]
     (str output-directory "/" (clean-author-name author-name) "-" suffix))
  ([output-directory author-name]
     (str output-directory "/" (clean-author-name author-name))))

(defn _scrape-abstracts
  "I don't understand these two functions yet... I think this is just
  a helper function."
  [uids basename options]
  (let [abstracts (retrieve-abstracts uids)
        get-writer (if (:split options)
                     (fn [abstract]
                       (io/writer (str basename "-" (:date abstract) ".txt")))
                     (fn [abstract]
                       (io/writer (str basename ".txt") :append true)))]
    (doseq [abstract abstracts]
      (with-open [wrtr (get-writer abstract)]
        (do (.write wrtr (:text abstract))
            (when (:date options) (.write wrtr (str "\n" (:date abstract))))
            (.write wrtr "\n\n"))))))

(defn scrape-abstracts
  "I don't understand these two functions yet..."
  [output-directory author-name options]
  (let [author-uids (retrieve-author-uids author-name)
        author-basename (base-filename output-directory author-name)]
    (_scrape-abstracts author-uids author-basename options)
    (when (:hop options)
      (let [related-uids (take (:limit options)
                               (retrieve-related-uids author-uids))
            related-basename (base-filename output-directory
                                            author-name
                                            "rel")]
        (_scrape-abstracts related-uids related-basename options)))))

(defn get-names-from-file
  "For the -i switch: read author names from a file. Assumes one
  author name per line."
  [filename]
  (with-open [rdr (io/reader filename)]
    (doall (map str/trim (line-seq rdr)))))

(defn run-with
  "Runs `scrape-abstracts` over the list of `author-names` and outputs
  to specified output. Options argument is a map of parsed CLI
  arguments. If output directory doesn't exist, it is created."
  [options]
  (let [output-directory (if (and (not= (:output options) ".")
                                  (.isDirectory (io/file (:output options))))
                           (:output options)
                           (do (.mkdir (java.io.File. (:output options)))
                               (:output options)))
        author-names     (into (:name options)
                               (if-let [filename (:input options)]
                                 (get-names-from-file filename)
                                 nil))]
    (doseq [author-name author-names]
      (scrape-abstracts output-directory author-name options))))

(defn -main [& args]
  (let [[options args banner] (parse-args args)]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (run-with options)))
