(ns entrez-vous.console
  (:gen-class :main true)
  (:use [clojure.tools.cli :only [cli]]))

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
       ["-e" "--email" "Identifying e-mail address for user."]
       ["-i" "--input" "Read author names from a text file."]
       ["-h" "--hops" "Specifies number of hops to take." :default 0 :parse-fn #(Integer. %)]
       ["-l" "--limit" "Limits number of additional authors." :default 0 :parse-fn #(Integer. %)]
       ["-o" "--output" "Choose an output directory." :default "."]))

(defn -main
  [& args]
  (let [[options args banner] (parse-args args)]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (println options)))
