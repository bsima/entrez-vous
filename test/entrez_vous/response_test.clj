(ns entrez-vous.response-test
  (:require [clojure.test :refer :all]
            [entrez-vous.response :refer :all]))

(deftest test-get-children
  (testing "Should return next-level children with appropriate tags."
    (is (= [{:tag :foo :content "x"}
            {:tag :foo :content "y"}
            {:tag :foo :content "z"}]
           (get-children { :tag :moo :content
                          [{ :content [{ :tag :foo :content "x" }] }
                           { :content [{ :tag :foo :content "y" }] }
                           { :content [{ :tag :foo :content "z" }] }] }
                         [:foo]
                         :content)))))

(deftest test-get-child
  (testing "Should select the first child of the correct type."
    (let [tree {:tag :parent
                :content [{:tag :bad :content ["Mayhem"]}
                          {:tag :bad :content ["Mischief"]}
                          {:tag :good :content ["Happiness"]}]}
          good-child (get-child tree :good)]
      (is (= :good (:tag good-child)))
      (is (= ["Happiness"] (:content good-child))))))

(deftest test-nested-get-child
  (testing "Should select correct children in a hierarchy."
    (let [tree {:tag :parent
                :content [{:tag :that
                           :content nil}
                          {:tag :this
                           :content [{:tag :bad
                                      :content nil}
                                     {:tag :good
                                      :content [{:tag :monster
                                                 :content nil}
                                                {:tag :child
                                                 :content ["Hooray!"]}]}]}]}
          deep-child (-> tree (get-child :this)
                              (get-child :good)
                              (get-child :child))]
      (is (= :child (:tag deep-child)))
      (is (= ["Hooray!"] (:content deep-child))))))
