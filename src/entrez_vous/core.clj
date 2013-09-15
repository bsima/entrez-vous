(ns entrez-vous.core
  (:require [org.httpkit.client :as http]))

(def session-webenv (atom nil))

(def session-query-keys (atom nil))

(def utility-base-url "eutils.ncbi.nlm.nih.gov/entrez/eutils")

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

(defn expand-name-for-query [name]
  (-> name (clojure.string/replace " " "+")
           (clojure.string/lower-case)
           (str "[author]")))

(defn build-query-string [parameter-map]
  (let [key-string (fn [k] (clojure.string/replace (str k) ":" ""))
        key-value-string (fn [[k v]] (str (key-string k) "=" v))]
    (clojure.string/join "&" (map key-value-string parameter-map))))

(defn make-request-url [utility-key request-parameters session-parameters]
  (let [utility-name (utility-key utility-names)
        utility-url (str utility-base-url "/" utility-name)
        query-parameter-map (reduce into [request-parameters
                                          session-parameters
                                          (:all utility-parameters)
                                          (utility-key utility-parameters)])
        query-string (build-query-string query-parameter-map)]
    (str utility-url "?" query-string)))



;; (http/get request-url { :keepalive 3000 :timeout 1000 }
;;           (fn [{:keys [status headers body error]}]
;;             (if error
;;               (println "Failed, exception is " error)
;;               (println "Async HTTP GET: " status))))
