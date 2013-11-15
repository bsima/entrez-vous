(ns entrez-vous.response)

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
