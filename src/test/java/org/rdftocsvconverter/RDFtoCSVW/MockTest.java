package org.rdftocsvconverter.RDFtoCSVW;


import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.rdftocsvconverter.RDFtoCSVW.enums.ParsingChoice;
import org.rdftocsvconverter.RDFtoCSVW.enums.TableChoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@AutoConfigureMockMvc
class MockTest {

    @Autowired
    private MockMvc mockMvc;

    private String testContent = """
                <http://example.org/foo> <http://example.org/bar> _:v .
                _:v <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> _:c .
                _:c <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Datatype> .""";
    private static final String fileName = "simpsons.ttl";
    private static final String table = String.valueOf(TableChoice.ONE);

    private static final String conversionMethod = String.valueOf(ParsingChoice.RDF4J);
    private static final String url = "https://w3c.github.io/csvw/tests/test005.ttl";

    @Test
    void rdftocsv_string_byUrl() throws Exception {
        // Perform GET request with URL parameters
        mockMvc.perform(get("/csv/string")
                        .param("url", url)
                        .param("table", table)
                        .param("conversionMethod", conversionMethod))

                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().string("Subject,FamilyName,Surname,child_id,id\n" +
                        "2,Simpson,Homer,3,1\n" +
                        "4,Simpson,Homer,4,1\n" +
                        "6,Simpson,Homer,5,1\n" +
                        "8,Simpson,Marge,3,2\n" +
                        "10,Simpson,Marge,4,2\n" +
                        "12,Simpson,Marge,5,2\n" +
                        "14,Simpson,Bart,,3\n" +
                        "16,Simpson,Lisa,,4\n" +
                        "18,Simpson,Maggie,,5\n" +
                        "20,Flanders,Ned,,6\n" +
                        "22,the Clown,Krusty,,7\n" +
                        "24,Smithers,Waylon,,8"));
    }

    @Test
    void rdftocsv_string_byFile() throws Exception {
        // Perform GET request with URL parameters
        // TODO
        mockMvc.perform(get("/csv/string")
                        .param("url", url)
                        .param("table", table))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().string("Subject,FamilyName,Surname,child_id,id\n" +
                        "2,Simpson,Homer,3,1\n" +
                        "4,Simpson,Homer,4,1\n" +
                        "6,Simpson,Homer,5,1\n" +
                        "8,Simpson,Marge,3,2\n" +
                        "10,Simpson,Marge,4,2\n" +
                        "12,Simpson,Marge,5,2\n" +
                        "14,Simpson,Bart,,3\n" +
                        "16,Simpson,Lisa,,4\n" +
                        "18,Simpson,Maggie,,5\n" +
                        "20,Flanders,Ned,,6\n" +
                        "22,the Clown,Krusty,,7\n" +
                        "24,Smithers,Waylon,,8"));
    }

    @Test
    void rdftocsvwmetadata_string_byUrl() throws Exception {
        // Perform GET request with URL parameters
        mockMvc.perform(get("/metadata/string")
                        .param("url", url)
                        .param("table", table))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().contentType("text/plain;charset=UTF-8"))  // Expect JSON content type
                .andExpect(jsonPath("$.tables[0].url").value("test005.csv"))  // Validate some JSON fields
                .andExpect(jsonPath("$.tables[0].tableSchema.columns[0].name").value("Subject"))  // Check field in JSON
                .andExpect(jsonPath("$.tables[0].tableSchema.columns[1].name").value("FamilyName"));  // Check another field
    }

    @Test
    void rdftocsvwmetadata_byUrl() throws Exception {
        // Perform GET request with URL parameters
        mockMvc.perform(get("/metadata")
                        .param("url", url)
                        .param("table", table))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().contentType("application/octet-stream"))  // Expect JSON content type
                .andExpect(jsonPath("$.tables[0].url").value("test005.csv"))  // Validate some JSON fields
                .andExpect(jsonPath("$.tables[0].tableSchema.columns[0].name").value("Subject"))  // Check field in JSON
                .andExpect(jsonPath("$.tables[0].tableSchema.columns[1].name").value("FamilyName"));  // Check another field
    }

    @Test

    void rdftocsvwmetadata_string_byFile() throws Exception {
        // Get the path to the resources folder
        String path = Paths.get("src", "test", "resources", fileName).toString();
        File file = new File(path);
        System.out.println(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        System.out.println(fileContent.toString());
        System.out.println(file.getName());
        System.out.println(file.toPath());

        // Create a MockMultipartFile from the local file
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons.ttl", "text/plain", fileContents.getBytes());

        String param1 = String.valueOf(TableChoice.ONE);

        // Perform POST request with file and parameters
        mockMvc.perform(multipart("/metadata/string")
                        .file(mockMultipartFile)
                        .param("table", param1))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().string("{\"@context\":\"http://www.w3.org/ns/csvw\",\"@type\":\"TableGroup\",\"tables\":[{\"@type\":\"Table\",\"url\":\"test005.csv\",\"tableSchema\":{\"@type\":\"Schema\",\"columns\":[{\"@type\":\"Column\",\"titles\":\"Subject\",\"name\":\"Subject\",\"valueUrl\":\"https://blank_Nodes_IRI.org/{+Subject}\",\"suppressOutput\":true},{\"@type\":\"Column\",\"titles\":\"FamilyName\",\"name\":\"FamilyName\",\"propertyUrl\":\"file://test005.csv#FamilyName\"},{\"@type\":\"Column\",\"titles\":\"Surname\",\"name\":\"Surname\",\"propertyUrl\":\"file://test005.csv#Surname\"},{\"@type\":\"Column\",\"titles\":\"child_id\",\"name\":\"child_id\",\"propertyUrl\":\"file://test005.csv#child_id\"},{\"@type\":\"Column\",\"titles\":\"id\",\"name\":\"id\",\"propertyUrl\":\"file://test005.csv#id\"},{\"@type\":\"Column\",\"titles\":\"Row Number\",\"name\":\"rowNum\",\"valueUrl\":\"{_row}\",\"datatype\":\"integer\",\"virtual\":true}],\"primaryKey\":\"Subject\",\"rowTitles\":[\"Subject\",\"FamilyName\",\"Surname\",\"child_id\",\"id\"]},\"transformations\":[{\"@type\":\"Template\",\"url\":\"https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/main/scripts/transformationForBlankNodesStreamed.js\",\"scriptFormat\":\"http://www.iana.org/assignments/media-types/application/javascript\",\"targetFormat\":\"http://www.iana.org/assignments/media-types/turtle\",\"source\":\"rdf\",\"titles\":\"RDF format used as the output format in the transformation from CSV to RDF\"}]}]}"));  // Check response content
    }

    @Test
    void rdftocsvwCSV_string_byFile() throws Exception {
        // Get the path to the resources folder
        String path = Paths.get("src", "test", "resources", fileName).toString();
        File file = new File(path);
        System.out.println(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        System.out.println(fileContent.toString());
        System.out.println(file.getName());
        System.out.println(file.toPath());

        // Create a MockMultipartFile from the local file
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons.ttl", "text/plain", fileContents.getBytes());

        String param1 = String.valueOf(TableChoice.ONE);

        // Perform POST request with file and parameters
        mockMvc.perform(multipart("/csv/string")
                        .file(mockMultipartFile)
                        .param("table", param1))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().string(expectedSimpsonsCSV));  // Check response content
    }

    @Test
    void rdftocsvwCSV_byFile() throws Exception {
        // Get the path to the resources folder
        String path = Paths.get("src", "test", "resources", fileName).toString();
        File file = new File(path);
        System.out.println(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        System.out.println(fileContent.toString());
        System.out.println(file.getName());
        System.out.println(file.toPath());

        // Create a MockMultipartFile from the local file
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons.ttl", "text/plain", fileContents.getBytes());

        String param1 = String.valueOf(TableChoice.ONE);

        // Perform POST request with file and parameters
        mockMvc.perform(multipart("/csv")
                        .file(mockMultipartFile)
                        .param("table", param1))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().string(notNullValue()));  // Check response content
    }
    @Test
    void rdftocsv_byUrl() throws Exception {
        // Perform GET request with URL parameters
        mockMvc.perform(get("/csv")
                        .param("url", url)
                        .param("table", table))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().string("Subject,FamilyName,Surname,child_id,id\n" +
                        "2,Simpson,Homer,3,1\n" +
                        "4,Simpson,Homer,4,1\n" +
                        "6,Simpson,Homer,5,1\n" +
                        "8,Simpson,Marge,3,2\n" +
                        "10,Simpson,Marge,4,2\n" +
                        "12,Simpson,Marge,5,2\n" +
                        "14,Simpson,Bart,,3\n" +
                        "16,Simpson,Lisa,,4\n" +
                        "18,Simpson,Maggie,,5\n" +
                        "20,Flanders,Ned,,6\n" +
                        "22,the Clown,Krusty,,7\n" +
                        "24,Smithers,Waylon,,8"));
    }

    @Test
    void rdftocsv_byUrl2() throws Exception {
        // Perform GET request with URL parameters
        mockMvc.perform(get("/csv")
                        .param("url", "https://w3c.github.io/csvw/tests/test005.ttl")
                        .param("table", table))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().string("Subject,FamilyName,Surname,child_id,id\n" +
                        "2,Simpson,Homer,3,1\n" +
                        "4,Simpson,Homer,4,1\n" +
                        "6,Simpson,Homer,5,1\n" +
                        "8,Simpson,Marge,3,2\n" +
                        "10,Simpson,Marge,4,2\n" +
                        "12,Simpson,Marge,5,2\n" +
                        "14,Simpson,Bart,,3\n" +
                        "16,Simpson,Lisa,,4\n" +
                        "18,Simpson,Maggie,,5\n" +
                        "20,Flanders,Ned,,6\n" +
                        "22,the Clown,Krusty,,7\n" +
                        "24,Smithers,Waylon,,8"));
    }

    private final String fileContents = "@prefix : <test005.csv#> .\n" +
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

    private final String expectedSimpsonsCSV = "Subject,FamilyName,Surname,child_id,id\n" +
            "2,Simpson,Homer,3,1\n" +
            "4,Simpson,Homer,4,1\n" +
            "6,Simpson,Homer,5,1\n" +
            "8,Simpson,Marge,3,2\n" +
            "10,Simpson,Marge,4,2\n" +
            "12,Simpson,Marge,5,2\n" +
            "14,Simpson,Bart,,3\n" +
            "16,Simpson,Lisa,,4\n" +
            "18,Simpson,Maggie,,5\n" +
            "20,Flanders,Ned,,6\n" +
            "22,the Clown,Krusty,,7\n" +
            "24,Smithers,Waylon,,8";

}

