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

(deftest test-unnest-children
  (testing "Should return the argued elements when no more levels."
    (is (= ["odelay"] (unnest-children ["odelay"] []))))
  (testing "Should return next-level children with appropriate tags."
    (is (= [{:tag :foo :content "x"}
            {:tag :foo :content "y"}
            {:tag :foo :content "z"}]
           (unnest-children [{ :content [{ :tag :foo :content "x" }]}
                             { :content [{ :tag :foo :content "y" }]}
                             { :content [{ :tag :foo :content "z" }]}]
                            [:foo])))))

(deftest test-extract-search-result-uids
  (testing "Should retrieve the UID's from an Entrez server Search response."
    (let [
          xml-string
          "<eSearchResult><Count>115</Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList><Id>777</Id><Id>888</Id><Id>999</Id></IdList></eSearchResult>"
          ]
      (is (= ["777" "888" "999"]
             (extract-search-result-uids xml-string))))))

(deftest test-extract-link-result-uids
  (testing "Should retrieve the UID's from an Entrez server Link response."
    (let [
          xml-string
          "<eLinkResult><LinkSet><DbFrom>pubmed</DbFrom><IdList><Id>23684593</Id></IdList><LinkSetDb><DbTo>pubmed</DbTo><LinkName>pubmed_pubmed</LinkName><Link><Id>555</Id><Score>26610558</Score></Link></LinkSetDb><LinkSetDb><DbTo>pubmed</DbTo><LinkName>pubmed_pubmed_combined</LinkName><Link><Id>666</Id><Score>26610558</Score></Link></LinkSetDb><LinkSetDb><DbTo>pubmed</DbTo><LinkName>pubmed_pubmed_five</LinkName><Link><Id>777</Id><Score>26610558</Score></Link></LinkSetDb><LinkSetDb><DbTo>pubmed</DbTo><LinkName>pubmed_pubmed_refs</LinkName><Link><Id>888</Id><Score>22542813</Score></Link></LinkSetDb></LinkSet></eLinkResult>"
          ]
      (is (= ["555" "666" "777" "888"]
             (extract-link-result-uids xml-string))))))

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

