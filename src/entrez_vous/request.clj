(ns entrez-vous.request)

(defn build-query-string [parameter-map]
  (let [key-string (fn [k] (clojure.string/replace (str k) ":" ""))
        key-value-string (fn [[k v]] (str (key-string k) "=" v))]
    (clojure.string/join "&" (map key-value-string parameter-map))))

(defn build-request-url [location resource parameter-map]
  (let [query-string (build-query-string parameter-map)]
    (str location "/" resource "?" query-string)))
