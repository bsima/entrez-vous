(ns entrez-vous.core
  (:require [entrez-vous.request :refer :all]
            [entrez-vous.response :refer :all]
            [org.httpkit.client :as http]
            [clojure.data.xml :as xml]))

(defn expand-name-for-query [name]
  (-> name (clojure.string/replace " " "+")
           (clojure.string/lower-case)
           (str "[author]")))

(defn get-in-xml [tag-sequence xml-string]
  (let [xml-data (xml/parse (java.io.StringReader. xml-string))]
    (get-children xml-data tag-sequence :content)))

(defn retrieve-author-uids [author]
  (let [request-parameters {:term (expand-name-for-query author)}
        request-url (build-request-url :search request-parameters)]
    (get-in-xml
     [:IdList :Id]
     (:body @(http/get request-url)))))

(defn retrieve-related-uids [original-uids]
  (let [request-parameters {:id (clojure.string/join "," original-uids)}
        request-url (build-request-url :link request-parameters)]
    (get-in-xml
     [:LinkSet :LinkSetDb :Link :Id]
     (:body @(http/post request-url)))))

(defn retrieve-abstracts [uids]
  (loop [remaining-uids uids
         abstracts-acc []]
    (if (not (empty? remaining-uids))
      (let [[head-uids, tail-uids] (split-at 10000 uids)
            request-url (build-request-url
                         :fetch
                         {:id (clojure.string/join "," head-uids)})
            abstract-texts (get-in-xml
                            [:PubmedArticle :MedlineCitation
                             :Article :Abstract :AbstractText]
                            (:body @(http/post request-url)))]
        (recur tail-uids (into abstracts-acc abstract-texts)))
      abstracts-acc)))
