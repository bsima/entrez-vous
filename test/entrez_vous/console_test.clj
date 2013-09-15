(ns entrez-vous.console-test
  (:require [clojure.test :refer :all]
            [entrez-vous.console :refer :all]))

(deftest test-parsing
  (testing "That the command line arguments work!"
    (let [[parsed-values _ _]
          (parse-args ["--name"   "Baron Davis"
                       "--name"   "Billy Joel"
                       "--input"  "qux.txt"
                       "--output" "/path/to"
                       "--help"
                       "--hop"])]
      (is (= (:help   parsed-values) true))
      (is (= (:hop    parsed-values) true))
      (is (= (:name   parsed-values) #{"Billy Joel" "Baron Davis"}))
      (is (= (:input  parsed-values) "qux.txt"))
      (is (= (:output parsed-values) "/path/to")))))
