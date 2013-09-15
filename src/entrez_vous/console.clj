(ns entrez-vous.console
  (:gen-class :main true)
  (:use [entrez-vous.core :only [retrieve-author-uids
                                 retrieve-related-uids
                                 retrieve-abstracts]]
        [clojure.tools.cli :only [cli]]
        [clojure.java.io]))

(defn parse-args
  [args]
  (cli args
       "This program scrapes author abstracts from NCBI Pubmed database."
       ["-h" "--help" "Prints the help banner." :default false :flag true]
       ["-n" "--name" "Add an author name to target."
        :assoc-fn (fn [previous key val]
                    (assoc previous key
                           (if-let [oldval (get previous key)]
                             (merge oldval val)
                             (hash-set val))))]
       ["-i" "--input" "Read author names from a text file."]
       ["-h" "--hop" "Hop to related papers." :default false :flag true]
       ["-o" "--output" "Choose an output directory." :default "."]))

(defn scrape-abstracts [author-name filename]
  (let [abstracts (retrieve-abstracts (retrieve-author-uids author-name))]
    (Thread/sleep 500)
    (with-open [wrtr (writer filename)]
      (doseq [abstract abstracts] (do (.write wrtr abstract)
                                      (.write wrtr "\n\n"))))))

(defn build-filename [output-directory author-name]
  (let [clean-author-name (clojure.string/lower-case
                           (clojure.string/replace author-name " " "-"))]
    (str output-directory "/" clean-author-name ".txt")))

(defn get-names-from-file [filename]
  (with-open [rdr (reader filename)]
    (doall (map clojure.string/trim (line-seq rdr)))))

(defn run-with [options]
  (let [output-directory (:output options)
        author-names (into (:name options)
                           (if-let [filename (:input options)]
                             (get-names-from-file filename)
                             nil))]
    (doseq [author-name author-names]
      (scrape-abstracts
       author-name
       (build-filename output-directory author-name)))))

(defn -main
  [& args]
  (let [[options args banner] (parse-args args)]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (run-with options)))
