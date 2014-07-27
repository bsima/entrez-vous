(defproject entrez-vous "0.1.1"
  :description "Entrez-Vous: A Clojure Project for Fun and Utility!"
  :url "https://github.com/bima/entrez-vous"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main entrez-vous.console
  :aot :all
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [http-kit "2.1.18"]
                 [org.clojure/data.xml "0.0.7"]])
