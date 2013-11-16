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
       ["-l" "--limit" "Limit # of relevant papers." :default 1000 :parse-fn #(Integer. %)]
       ["-o" "--output" "Choose an output directory." :default "."]))

(defn clean-author-name [author-name]
  (clojure.string/lower-case
   (clojure.string/replace author-name " " "-")))

(defn build-filename
  ([output-directory author-name suffix]
     (str output-directory "/" (clean-author-name author-name) "-" suffix ".txt"))
  ([output-directory author-name]
     (str output-directory "/" (clean-author-name author-name) ".txt")))

(defn _scrape-abstracts [uids filename]
  (let [abstracts (retrieve-abstracts uids)]
    (with-open [wrtr (writer filename)]
      (doseq [abstract abstracts]
        (do (.write wrtr (:text abstract))
            (.write wrtr (str "\n" (:date abstract)))
            (.write wrtr "\n\n"))))))

(defn scrape-abstracts [output-directory author-name options]
  (let [author-uids (retrieve-author-uids author-name)
        author-filename (build-filename output-directory author-name)]
    (_scrape-abstracts author-uids author-filename)
    (when (:hop options)
      (let [related-uids (take (:limit options)
                               (retrieve-related-uids author-uids))
            related-filename (build-filename output-directory
                                             author-name
                                             "rel")]
        (_scrape-abstracts related-uids related-filename)))))

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
      (scrape-abstracts output-directory author-name options))))

(defn -main [& args]
  (let [[options args banner] (parse-args args)]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (run-with options)))
