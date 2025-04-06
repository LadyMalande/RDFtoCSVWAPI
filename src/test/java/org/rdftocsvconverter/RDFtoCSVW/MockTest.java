package org.rdftocsvconverter.RDFtoCSVW;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
class MockTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    private final String simpsons = """
            Subject,FamilyName,Surname,child_id,id
            2,Simpson,Homer,3,1
            4,Simpson,Homer,4,1
            6,Simpson,Homer,5,1
            8,Simpson,Marge,3,2
            10,Simpson,Marge,4,2
            12,Simpson,Marge,5,2
            14,Simpson,Bart,,3
            16,Simpson,Lisa,,4
            18,Simpson,Maggie,,5
            20,Flanders,Ned,,6
            22,the Clown,Krusty,,7
            24,Smithers,Waylon,,8""";
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
                        .param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)))

                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().string(simpsons));
    }

    @Test
    void rdftocsv_string_byFile() throws Exception {
        // Perform GET request with URL parameters
        // TODO
        mockMvc.perform(get("/csv/string")
                        .param("url", url)
                        .param("table", table))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().string(simpsons));
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
    // TODO test not passing
    void rdftocsvwmetadata_string_byFile() throws Exception {
        // Get the path to the resources folder
        String path = Paths.get("src", "test", "resources", fileName).toString();
        File file = new File(path);
        System.out.println(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        System.out.println(Arrays.toString(fileContent));
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
        System.out.println(Arrays.toString(fileContent));
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
                .andExpect(content().string(simpsons));  // Check response content
    }

    @Test
    void rdftocsvwCSV_byFile() throws Exception {
        // Get the path to the resources folder
        String path = Paths.get("src", "test", "resources", fileName).toString();
        File file = new File(path);
        System.out.println(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        System.out.println(Arrays.toString(fileContent));
        System.out.println(file.getName());
        System.out.println(file.toPath());

        // Create a MockMultipartFile from the local file
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons2.ttl", "text/plain", fileContents.getBytes());

        String param1 = String.valueOf(TableChoice.MORE);

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
                .andExpect(content().string(simpsons));
    }

    @Test
    void rdftocsv_byUrl2() throws Exception {
        // Perform GET request with URL parameters
        mockMvc.perform(get("/csv")
                        .param("url", "https://w3c.github.io/csvw/tests/test005.ttl")
                        .param("table", table))
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().string(simpsons));
    }

}

