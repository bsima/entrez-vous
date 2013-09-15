(ns entrez-vous.core-test
  (:require [clojure.test :refer :all]
            [entrez-vous.core :refer :all]))

(deftest test-expand-name-for-query
  (testing "Should lower-case and replace whitepace with '+' character."
    (is (= "herman+miller+aeron[author]"
           (expand-name-for-query "Herman Miller Aeron")))))

(deftest test-build-query-string
  (testing "Should join elements into a querystring with (=,?)."
    (is (= "nom=food&hey=lady"
           (build-query-string [[:nom "food"] [:hey "lady"]])))))

(deftest test-get-search-result-uids
  (testing "Should retrieve the UID's from an Entrez server response."
    (let [
xml-string
"<eSearchResult><Count>115</Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList><Id>777</Id><Id>888</Id><Id>999</Id></IdList></eSearchResult>"
          ]
      (is (= ["777" "888" "999"]
             (get-search-result-uids xml-string))))))
