(ns entrez-vous.response
  (:require [clojure.data.xml :as xml]))

(defn- eq-or-in [a b] (or (= b a) (= a (b a))))

(defn get-child [parent tag]
  (first (filter #(eq-or-in (:tag %) tag) (:content parent))))

(defn get-children [root tag-sequence children-key]
  (loop [elems (children-key root)
         tags  tag-sequence]
    (if (not (empty? tags))
      (let [[tag & remaining-tags] tags
            next-elems (mapcat children-key
                               (filter #(eq-or-in (:tag %) tag) elems))]
        (recur next-elems remaining-tags))
      elems)))

(defn get-tag-sequence-data [tag-sequence content-key xml-string]
  (let [xml-data (xml/parse (java.io.StringReader. xml-string))]
    (get-children xml-data tag-sequence content-key)))

(defn get-tag-sequence-content [tag-sequence xml-string]
  (get-tag-sequence-data tag-sequence :content xml-string))

