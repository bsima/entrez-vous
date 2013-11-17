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
       ["-d" "--date" "Append date-stamps." :default false :flag true]
       ["-s" "--split" "Split by articles." :default false :flag true]
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

(defn base-filename
  ([output-directory author-name suffix]
     (str output-directory "/" (clean-author-name author-name) "-" suffix))
  ([output-directory author-name]
     (str output-directory "/" (clean-author-name author-name))))

(defn _scrape-abstracts [uids basename options]
  (let [abstracts (retrieve-abstracts uids)
        get-writer (fn [abstract]
                     (if (:split options)
                       (writer (str basename "-" (:date abstract) ".txt"))
                       (writer (str basename ".txt") :append true)))]
    (doseq [abstract abstracts]
      (with-open [wrtr (get-writer abstract)]
        (do (.write wrtr (:text abstract))
            (when (:date options) (.write wrtr (str "\n" (:date abstract))))
            (.write wrtr "\n\n"))))))

(defn scrape-abstracts [output-directory author-name options]
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
