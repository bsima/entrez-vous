(ns entrez-vous.request-test
  (:require [clojure.test :refer :all]
            [entrez-vous.request :refer :all]))

(deftest test-build-query-string
  (testing "Should join elements into a querystring with (=,?)."
    (is (= "nom=food&hey=lady"
           (build-query-string [[:nom "food"] [:hey "lady"]])))))
