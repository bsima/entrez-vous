(defproject entrez-vous "0.1.0-SNAPSHOT"
  :description "Entrez-Vous: A Clojure Project for Fun and Utility!"
  :url "https://github.com/triposorbust/entrez-vous"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main entrez-vous.console
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.4"]
                 [http-kit "2.1.10"]
                 [org.clojure/data.xml "0.0.7"]])
