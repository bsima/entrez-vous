(ns entrez-vous.console-test
  (:require [clojure.test :refer :all]
            [entrez-vous.console :refer :all]))

(deftest test-parsing
  (testing "That the command line arguments work!"
    (let [[parsed-values _ _]
          (parse-args ["--name"   "Baron Davis"
                       "--name"   "Billy Joel"
                       "--email"  "foo@bar.baz"
                       "--input"  "qux.txt"
                       "--hops"   "10"
                       "--limit"  "11"
                       "--output" "/path/to"
                       "--help"])]
      (is (= (:help   parsed-values) true))
      (is (= (:name   parsed-values) #{"Billy Joel" "Baron Davis"}))
      (is (= (:email  parsed-values) "foo@bar.baz"))
      (is (= (:hops   parsed-values) 10))
      (is (= (:input  parsed-values) "qux.txt"))
      (is (= (:limit  parsed-values) 11))
      (is (= (:output parsed-values) "/path/to")))))
