(ns entrez-vous.core
  (:require [org.httpkit.client :as http]
            [clojure.data.xml :as xml]))

(def utility-base-url "http://eutils.ncbi.nlm.nih.gov/entrez/eutils")

(def utility-names
  { :search "esearch.fcgi"
    :fetch "efetch.fcgi"
    :link "elink.fcgi" })

(def utility-parameters
  {
   :all { :tool "entrez-vous" :db "pubmed" }
   :fetch { :retmode "xml" :rettype "abstract" :retmax "10000" :retstart "0" }
   :link { :dbfrom "pubmed" :cmd "neighbor_score" :linkname "pubmed_pubmed" }
   :search { :retstart "0" :retmax "100000" }
   })

(defn expand-name-for-query [name]
  (-> name (clojure.string/replace " " "+")
           (clojure.string/lower-case)
           (str "[author]")))

(defn build-query-string [parameter-map]
  (let [key-string (fn [k] (clojure.string/replace (str k) ":" ""))
        key-value-string (fn [[k v]] (str (key-string k) "=" v))]
    (clojure.string/join "&" (map key-value-string parameter-map))))

(defn make-request-url [utility-key request-parameters]
  (let [utility-name (utility-key utility-names)
        utility-url (str utility-base-url "/" utility-name)
        query-parameter-map (reduce into [request-parameters
                                          (:all utility-parameters)
                                          (utility-key utility-parameters)])
        query-string (build-query-string query-parameter-map)]
    (str utility-url "?" query-string)))

(defn unnest-children [elems tag-sequence]
  (if (not (empty? tag-sequence))
    (let [[tag & remaining-tags] tag-sequence
          flat-contents (apply concat (for [e elems] (:content e)))
          tag-children (filter (fn [e] (= tag (:tag e))) flat-contents)]
      (recur tag-children remaining-tags))
    elems))

(defn get-tag-sequence-content [tag-sequence xml-string]
  (let [xml-data (xml/parse (java.io.StringReader. xml-string))
        children (unnest-children [xml-data] tag-sequence)]
    (mapcat #(:content %) children)))

(def extract-search-result-uids
  (partial get-tag-sequence-content
           [:IdList :Id]))

(def extract-link-result-uids
  (partial get-tag-sequence-content
           [:LinkSet :LinkSetDb :Link :Id]))

(def extract-fetched-abstracts
  (partial get-tag-sequence-content
           [:PubmedArticle :MedlineCitation :Article :Abstract :AbstractText]))

(defn retrieve-author-uids [author]
  (let [request-parameters {:term (expand-name-for-query author)}
        request-url (make-request-url :search request-parameters)]
    (extract-search-result-uids
     (:body @(http/get request-url)))))

(defn retrieve-related-uids [original-uids]
  (let [request-parameters {:id (clojure.string/join "," original-uids)}
        request-url (make-request-url :link request-parameters)]
    (extract-link-result-uids
     (:body @(http/post request-url)))))

(defn retrieve-abstracts [uids]
  (loop [remaining-uids uids
         abstracts-acc []]
    (if (not (empty? remaining-uids))
      (let [head-uids (take 10000 uids)
            tail-uids (drop 10000 uids)
            request-parameters {:id (clojure.string/join "," head-uids)}
            request-url (make-request-url :fetch request-parameters)
            abstracts (extract-fetched-abstracts
                       (:body @(http/post request-url)))]
        (println request-url)
        (recur tail-uids (into abstracts-acc abstracts)))
      abstracts-acc)))
