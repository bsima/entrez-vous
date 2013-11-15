(ns entrez-vous.core
  (:require [entrez-vous.request :refer :all]
            [entrez-vous.response :refer :all]
            [org.httpkit.client :as http]
            [clojure.data.xml :as xml]))

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


(defn build-entrez-request-url [resource-key request-parameter-map]
  (let [utility-resource-url (resource-key utility-name-map)
        parameter-map (reduce into [request-parameter-map
                                    (:all utility-parameter-map)
                                    (resource-key utility-parameter-map)])]
    (build-request-url utility-base-url
                       utility-resource-url
                       parameter-map)))

(defn expand-name-for-query [name]
  (-> name (clojure.string/replace " " "+")
           (clojure.string/lower-case)
           (str "[author]")))

(defn get-in-xml [tag-sequence xml-string]
  (let [xml-data (xml/parse (java.io.StringReader. xml-string))]
    (get-children xml-data tag-sequence :content)))

(defn retrieve-author-uids [author]
  (let [request-parameters {:term (expand-name-for-query author)}
        request-url (build-entrez-request-url :search request-parameters)]
    (Thread/sleep 100)
    (get-in-xml
     [:IdList :Id]
     (:body @(http/get request-url)))))

(defn retrieve-related-uids [original-uids]
  (let [request-parameters {:id (clojure.string/join "," original-uids)}
        request-url (build-entrez-request-url :link request-parameters)
        ret-bod (:body @(http/post request-url))]
    (Thread/sleep 100)
    (get-in-xml
     [:LinkSet :LinkSetDb :Link :Id]
     (:body @(http/post request-url)))))

(defn retrieve-abstracts [uids]
  (loop [remaining-uids uids
         abstracts-acc []]
    (if (not (empty? remaining-uids))
      (let [[head-uids, tail-uids] (split-at 250 remaining-uids)
            request-url (build-entrez-request-url
                         :fetch
                         {:id (clojure.string/join "," head-uids)})
            abstract-texts (get-in-xml
                            [:PubmedArticle :MedlineCitation
                             :Article :Abstract :AbstractText]
                            (:body @(http/post request-url)))]
        (Thread/sleep 100)
        (recur tail-uids (into abstracts-acc abstract-texts)))
      abstracts-acc)))
