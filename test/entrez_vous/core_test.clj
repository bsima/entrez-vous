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

