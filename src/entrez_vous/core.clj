(ns entrez-vous.core
  (:require [org.httpkit.client :as http]))

(def default-parameters
  {})

(defn clean-name
  [name]
  (-> name
      (clojure.string/replace " " "+")
      (clojure.string/lower-case)))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
