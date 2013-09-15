(ns entrez-vous.core
  (:require [org.httpkit.client :as http]))

(def utility-base-url "eutils.ncbi.nlm.nih.gov/entrez/eutils")

(def utility-webenv (atom nil))

(def utility-names
  { :search "esearch.fcgi"
    :fetch "efetch.fcgi"
    :link "elink.fcgi"
    :post "epost.fcgi" })

(def utility-parameters
  {
   :all { :tool "entrez-vous-abstract-retriever" :db "pubmed" :usehistory "y" }
   :fetch { :retmode "xml" :rettype "abstract" :retmax "10000" :retstart "0" }
   :link { :dbfrom "pubmed" :cmd "neighbor_score" :linkname "pubmed_pubmed" }
   :search { :retstart "0" :retmax "100000" }
   :post { }
   })

;; http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=George+hripcsak[author]
;; http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=23684593,23975625
;;
;; Per session stuff:
;;
;; query_key
;; WebEnv
;; 

(defn expand-name-for-query [name]
  (-> name (clojure.string/replace " " "+")
           (clojure.string/lower-case)
           (str "[author]")))

(defn build-query-string [parameter-map]
  (let [kw-string (fn [k] (clojure.string/replace (str k) ":" ""))
        kv-string (fn [[k v]] (str (kw-string k) "=" v))]
    (clojure.string/join "&" (map kv-string parameter-map))))

