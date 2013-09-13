(ns entrez-vous.core-test
  (:require [clojure.test :refer :all]
            [entrez-vous.core :refer :all]))

(deftest test-clean-name
  (testing "Should lower-case and replace whitepace with '+' character."
    (is (= "herman+miller+aeron" (clean-name "Herman Miller Aeron")))))

(deftest test-build-query-string
  (testing "Joined into a querystring (=,?)."
    (is (= "nom=food&hey=lady"
           (build-query-string [[:nom "food"] [:hey "lady"]])))))
