(ns entrez-vous.request)

(def utility-base-url "http://eutils.ncbi.nlm.nih.gov/entrez/eutils")

(def utility-name-map
  {:search "esearch.fcgi"
   :fetch "efetch.fcgi"
   :link "elink.fcgi"})

(def utility-parameter-map
  {:all { :tool "entrez-vous" :db "pubmed" }
   :fetch { :retmode "xml" :rettype "abstract" :retmax "10000" :retstart "0" }
   :link { :dbfrom "pubmed" :cmd "neighbor_score" :linkname "pubmed_pubmed" }
   :search { :retstart "0" :retmax "100000" }})

(defn build-query-string [parameter-map]
  (let [key-string (fn [k] (clojure.string/replace (str k) ":" ""))
        key-value-string (fn [[k v]] (str (key-string k) "=" v))]
    (clojure.string/join "&" (map key-value-string parameter-map))))

(defn build-request-url [utility-key request-parameters]
  (let [utility-name (utility-key utility-name-map)
        query-string (build-query-string
                      (reduce into [request-parameters
                                    (:all utility-parameter-map)
                                    (utility-key utility-parameter-map)]))]
    (str utility-base-url "/" utility-name "?" query-string)))
