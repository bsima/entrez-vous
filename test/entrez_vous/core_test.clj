(ns entrez-vous.core-test
  (:require [clojure.test :refer :all]
            [entrez-vous.core :refer :all]))

(deftest test-expand-name-for-query
  (testing "Should lower-case and replace whitepace with '+' character."
    (is (= "herman+miller+aeron[author]"
           (expand-name-for-query "Herman Miller Aeron")))))

(deftest test-extract-article-date
  (testing "Should enable multiple selections with a keyset."
    (let [xml-string "<Article>
                        <ArticleDate>
                          <Year>2013</Year>
                          <Month>11</Month>
                          <Day>06</Day>
                        </ArticleDate>
                      </Article>"]
      (is (= ["2013" "11" "06"]
             (get-in-xml [:ArticleDate
                          (sorted-set :Year :Month :Day)]
                         xml-string))))))

(deftest test-extract-search-result-uids
  (testing "Should retrieve the UID's from an Entrez server Search response."
    (let [xml-string "<eSearchResult>
                        <Count>115</Count>
                        <RetMax>20</RetMax>
                        <RetStart>0</RetStart>
                        <IdList>
                          <Id>777</Id>
                          <Id>888</Id>
                          <Id>999</Id>
                        </IdList>
                      </eSearchResult>"]
      (is (= ["777" "888" "999"]
             (get-in-xml [:IdList :Id]
                         xml-string))))))

(deftest test-extract-link-result-uids
  (testing "Should retrieve the UID's from an Entrez server Link response."
    (let [xml-string "<eLinkResult>
                        <LinkSet>
                          <DbFrom>pubmed</DbFrom>
                          <IdList>
                            <Id>23684593</Id>
                          </IdList>
                          <LinkSetDb>
                            <DbTo>pubmed</DbTo>
                            <LinkName>pubmed_pubmed</LinkName>
                            <Link>
                              <Id>555</Id>
                              <Score>26610558</Score>
                            </Link>
                          </LinkSetDb>
                          <LinkSetDb>
                            <DbTo>pubmed</DbTo>
                            <LinkName>pubmed_pubmed_combined</LinkName>
                            <Link>
                              <Id>666</Id>
                              <Score>26610558</Score>
                            </Link>
                          </LinkSetDb>
                          <LinkSetDb>
                            <DbTo>pubmed</DbTo>
                            <LinkName>pubmed_pubmed_five</LinkName>
                            <Link>
                              <Id>777</Id>
                              <Score>26610558</Score>
                            </Link>
                          </LinkSetDb>
                          <LinkSetDb>
                            <DbTo>pubmed</DbTo>
                            <LinkName>pubmed_pubmed_refs</LinkName>
                            <Link>
                              <Id>888</Id>
                              <Score>22542813</Score>
                            </Link>
                          </LinkSetDb>
                        </LinkSet>
                      </eLinkResult>"]
      (is (= ["555" "666" "777" "888"]
             (get-in-xml [:LinkSet :LinkSetDb :Link :Id]
                         xml-string))))))
