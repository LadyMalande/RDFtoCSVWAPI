package org.rdftocsvconverter.RDFtoCSVW;

public class BaseTest {
    public static final String fileContents = "@prefix : <test005.csv#> .\n" +
            "@prefix csvw: <http://www.w3.org/ns/csvw#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "\n" +
            " [\n" +
            "    a csvw:TableGroup;\n" +
            "    csvw:table [\n" +
            "      a csvw:Table;\n" +
            "      csvw:row [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Homer\";\n" +
            "          :child_id \"3\";\n" +
            "          :id \"1\"\n" +
            "        ];\n" +
            "        csvw:rownum 1;\n" +
            "        csvw:url <test005.csv#row=2>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Homer\";\n" +
            "          :child_id \"4\";\n" +
            "          :id \"1\"\n" +
            "        ];\n" +
            "        csvw:rownum 2;\n" +
            "        csvw:url <test005.csv#row=3>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Homer\";\n" +
            "          :child_id \"5\";\n" +
            "          :id \"1\"\n" +
            "        ];\n" +
            "        csvw:rownum 3;\n" +
            "        csvw:url <test005.csv#row=4>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Marge\";\n" +
            "          :child_id \"3\";\n" +
            "          :id \"2\"\n" +
            "        ];\n" +
            "        csvw:rownum 4;\n" +
            "        csvw:url <test005.csv#row=5>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Marge\";\n" +
            "          :child_id \"4\";\n" +
            "          :id \"2\"\n" +
            "        ];\n" +
            "        csvw:rownum 5;\n" +
            "        csvw:url <test005.csv#row=6>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Marge\";\n" +
            "          :child_id \"5\";\n" +
            "          :id \"2\"\n" +
            "        ];\n" +
            "        csvw:rownum 6;\n" +
            "        csvw:url <test005.csv#row=7>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Bart\";\n" +
            "          :id \"3\"\n" +
            "        ];\n" +
            "        csvw:rownum 7;\n" +
            "        csvw:url <test005.csv#row=8>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Lisa\";\n" +
            "          :id \"4\"\n" +
            "        ];\n" +
            "        csvw:rownum 8;\n" +
            "        csvw:url <test005.csv#row=9>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Simpson\";\n" +
            "          :Surname \"Maggie\";\n" +
            "          :id \"5\"\n" +
            "        ];\n" +
            "        csvw:rownum 9;\n" +
            "        csvw:url <test005.csv#row=10>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Flanders\";\n" +
            "          :Surname \"Ned\";\n" +
            "          :id \"6\"\n" +
            "        ];\n" +
            "        csvw:rownum 10;\n" +
            "        csvw:url <test005.csv#row=11>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"the Clown\";\n" +
            "          :Surname \"Krusty\";\n" +
            "          :id \"7\"\n" +
            "        ];\n" +
            "        csvw:rownum 11;\n" +
            "        csvw:url <test005.csv#row=12>\n" +
            "      ],  [\n" +
            "        a csvw:Row;\n" +
            "        csvw:describes [\n" +
            "          :FamilyName \"Smithers\";\n" +
            "          :Surname \"Waylon\";\n" +
            "          :id \"8\"\n" +
            "        ];\n" +
            "        csvw:rownum 12;\n" +
            "        csvw:url <test005.csv#row=13>\n" +
            "      ];\n" +
            "      csvw:url <test005.csv>\n" +
            "    ]\n" +
            " ] .\n";
}
