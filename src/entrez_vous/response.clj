(ns entrez-vous.response
  (:require [clojure.data.xml :as xml]))

(defn unnest-children [elems tag-sequence]
  (if (not (empty? tag-sequence))
    (let [[tag & remaining-tags] tag-sequence
          flat-contents (apply concat (for [e elems] (:content e)))
          tag-children (filter #(or (= tag (:tag %))
                                    (not (nil? (tag (:tag %)))))
                               flat-contents)]
      (recur tag-children remaining-tags))
    elems))

(defn get-tag-sequence-data [tag-sequence conversion-function xml-string]
  (let [xml-data (xml/parse (java.io.StringReader. xml-string))
        children (unnest-children [xml-data] tag-sequence)]
    (mapcat conversion-function children)))

(defn get-tag-sequence-content [tag-sequence xml-string]
  (get-tag-sequence-data tag-sequence #(:content %) xml-string))

(defn get-child [parent tag]
  (first (filter #(or (= tag (:tag %))
                      (not (nil? (tag (:tag %)))))
                 (:content parent))))
