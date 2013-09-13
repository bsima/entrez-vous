(ns entrez-vous.core
  (:require [org.httpkit.client :as http]))

(def default-parameters
  {})

(defn clean-name
  [name]
  (-> name
      (clojure.string/replace " " "+")
      (clojure.string/lower-case)))

(defn build-query-string
  [options]
  (let [kw-string (fn [k] (clojure.string/replace (str k) ":" ""))
        kv-string (fn [[k v]] (str (kw-string k) "=" v))]
    (clojure.string/join "&" (map kv-string options))))
