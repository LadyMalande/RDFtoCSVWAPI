package org.rdftocsvconverter.RDFtoCSVW;

/**
 * The Base class for tests offering common field.
 */
public class BaseTest {
    /**
     * The constant fileContents.
     */
    public static final String fileContents = """
            @prefix : <test005.csv#> .
            @prefix csvw: <http://www.w3.org/ns/csvw#> .
            @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

             [
                a csvw:TableGroup;
                csvw:table [
                  a csvw:Table;
                  csvw:row [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Homer";
                      :child_id "3";
                      :id "1"
                    ];
                    csvw:rownum 1;
                    csvw:url <test005.csv#row=2>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Homer";
                      :child_id "4";
                      :id "1"
                    ];
                    csvw:rownum 2;
                    csvw:url <test005.csv#row=3>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Homer";
                      :child_id "5";
                      :id "1"
                    ];
                    csvw:rownum 3;
                    csvw:url <test005.csv#row=4>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Marge";
                      :child_id "3";
                      :id "2"
                    ];
                    csvw:rownum 4;
                    csvw:url <test005.csv#row=5>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Marge";
                      :child_id "4";
                      :id "2"
                    ];
                    csvw:rownum 5;
                    csvw:url <test005.csv#row=6>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Marge";
                      :child_id "5";
                      :id "2"
                    ];
                    csvw:rownum 6;
                    csvw:url <test005.csv#row=7>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Bart";
                      :id "3"
                    ];
                    csvw:rownum 7;
                    csvw:url <test005.csv#row=8>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Lisa";
                      :id "4"
                    ];
                    csvw:rownum 8;
                    csvw:url <test005.csv#row=9>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Simpson";
                      :Surname "Maggie";
                      :id "5"
                    ];
                    csvw:rownum 9;
                    csvw:url <test005.csv#row=10>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Flanders";
                      :Surname "Ned";
                      :id "6"
                    ];
                    csvw:rownum 10;
                    csvw:url <test005.csv#row=11>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "the Clown";
                      :Surname "Krusty";
                      :id "7"
                    ];
                    csvw:rownum 11;
                    csvw:url <test005.csv#row=12>
                  ],  [
                    a csvw:Row;
                    csvw:describes [
                      :FamilyName "Smithers";
                      :Surname "Waylon";
                      :id "8"
                    ];
                    csvw:rownum 12;
                    csvw:url <test005.csv#row=13>
                  ];
                  csvw:url <test005.csv>
                ]
             ] .
            """;
}
