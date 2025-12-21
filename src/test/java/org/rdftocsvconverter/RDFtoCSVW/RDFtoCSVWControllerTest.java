package org.rdftocsvconverter.RDFtoCSVW;

import com.miklosova.rdftocsvw.support.AppConfig;
import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.rdftocsvconverter.RDFtoCSVW.api.controller.RDFtoCSVWController;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(RDFtoCSVWController.class)
class RDFtoCSVWControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RDFtoCSVWService rdFtoCSVWService;

    @Test
    void testGetCSVW_withFileUpload_shouldReturnZip() throws Exception {
        // Load test file from resources
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.ttl", "text/turtle", testFileBytes);

        // Mock service to return dummy ZIP bytes
        byte[] fakeZipContent = "dummy zip content".getBytes(StandardCharsets.UTF_8);
        when(rdFtoCSVWService.getCSVW(any(), any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(fakeZipContent));

        // Perform request
        mockMvc.perform(multipart("/rdftocsvw")
                        .file(file)
                        .param("choice", "RDF4J")
                        .param("tables", "ONE")
                        .param("firstNormalForm", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(fakeZipContent));
    }

    @Test
    void testGetCSVW_withFileUrl_shouldReturnZip() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "filename.ttl", "text/turtle", "<some RDF>".getBytes()
        );
        byte[] fakeZipContent = "dummy zip content".getBytes(StandardCharsets.UTF_8);
        when(rdFtoCSVWService.getCSVW(isNull(), eq(fakeURL), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(fakeZipContent));

        mockMvc.perform(post("/rdftocsvw")
                        //.file(mockFile)
                        .param("fileURL", fakeURL)
                        .param("choice", "RDF4J")
                        .param("tables", "ONE")
                        .param("firstNormalForm", "true"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fakeZipContent));
    }

    @Test
    void contextLoads() {
        // If it fails here, the context isn't loading properly
    }

    // Tests for AppConfig-based endpoints

    @Test
    void testGetCSVString_withUrl_shouldReturnCSVString() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        String expectedCSV = "column1,column2\nvalue1,value2";
        
        // Create real AppConfig
        AppConfig config = new AppConfig.Builder(fakeURL)
                .multipleTables(false)  // ONE table
                .parsing("RDF4J")
                .firstNormalForm(false)
                .preferredLanguages("en,cs")
                .columnNamingConvention("camelCase")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getCSVString to return expected CSV
        when(rdFtoCSVWService.getCSVString(any(AppConfig.class)))
                .thenReturn(expectedCSV);

        mockMvc.perform(get("/csv/string")
                        .param("url", fakeURL)
                        .param("table", "ONE")
                        .param("conversionMethod", "RDF4J")
                        .param("firstNormalForm", "false")
                        .param("preferredLanguages", "en,cs")
                        .param("namingConvention", "camelCase"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedCSV));
    }

    @Test
    void testGetCSVString_withFile_shouldReturnCSVString() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        String expectedCSV = "column1,column2\nvalue1,value2";
        
        // Create real AppConfig (file will be set by service)
        AppConfig config = new AppConfig.Builder("src/test/resources/RDFFeatures/basic_triple.nt")
                .multipleTables(false)  // ONE table
                .parsing("RDF4J")
                .firstNormalForm(false)
                .preferredLanguages("en")
                .columnNamingConvention("snake_case")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getCSVStringFromFile to return expected CSV
        when(rdFtoCSVWService.getCSVStringFromFile(any(AppConfig.class)))
                .thenReturn(expectedCSV);

        mockMvc.perform(multipart("/csv/string")
                        .file(file)
                        .param("table", "ONE")
                        .param("conversionMethod", "RDF4J")
                        .param("firstNormalForm", "false")
                        .param("preferredLanguages", "en")
                        .param("namingConvention", "snake_case"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedCSV));
    }

    @Test
    void testGetCSVFile_withUrl_shouldReturnCSVFile() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        byte[] expectedCSVBytes = "column1,column2\nvalue1,value2".getBytes(StandardCharsets.UTF_8);
        
        // Create real AppConfig
        AppConfig config = new AppConfig.Builder(fakeURL)
                .multipleTables(false)  // ONE table
                .parsing("RDF4J")
                .firstNormalForm(true)
                .preferredLanguages("en,de")
                .columnNamingConvention("PascalCase")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getCSVFileFromURL to return expected CSV bytes
        when(rdFtoCSVWService.getCSVFileFromURL(any(AppConfig.class)))
                .thenReturn(expectedCSVBytes);

        mockMvc.perform(get("/csv")
                        .param("url", fakeURL)
                        .param("table", "ONE")
                        .param("conversionMethod", "RDF4J")
                        .param("firstNormalForm", "true")
                        .param("preferredLanguages", "en,de")
                        .param("namingConvention", "PascalCase"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(expectedCSVBytes));
    }

    @Test
    void testGetCSVFile_withFile_shouldReturnCSVFile() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        byte[] expectedCSVBytes = "column1,column2\nvalue1,value2".getBytes(StandardCharsets.UTF_8);
        
        // Create real AppConfig
        AppConfig config = new AppConfig.Builder("src/test/resources/RDFFeatures/basic_triple.nt")
                .multipleTables(true)  // MORE tables
                .parsing("STREAMING")
                .firstNormalForm(false)
                .columnNamingConvention("kebab-case")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getCSVFileFromFile to return expected CSV bytes
        when(rdFtoCSVWService.getCSVFileFromFile(any(AppConfig.class)))
                .thenReturn(expectedCSVBytes);

        mockMvc.perform(multipart("/csv")
                        .file(file)
                        .param("table", "MORE")
                        .param("conversionMethod", "STREAMING")
                        .param("firstNormalForm", "false")
                        .param("namingConvention", "kebab-case"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(expectedCSVBytes));
    }

    @Test
    void testGetMetadataString_withUrl_shouldReturnMetadataJSON() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        String expectedMetadata = "{\"@context\": \"http://www.w3.org/ns/csvw\"}";
        
        // Create real AppConfig
        AppConfig config = new AppConfig.Builder(fakeURL)
                .multipleTables(false)  // ONE table
                .parsing("RDF4J")
                .firstNormalForm(false)
                .preferredLanguages("en")
                .columnNamingConvention("camelCase")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getMetadataString to return expected metadata
        when(rdFtoCSVWService.getMetadataString(any(AppConfig.class)))
                .thenReturn(expectedMetadata);

        mockMvc.perform(get("/metadata/string")
                        .param("url", fakeURL)
                        .param("table", "ONE")
                        .param("conversionMethod", "RDF4J")
                        .param("firstNormalForm", "false")
                        .param("preferredLanguages", "en")
                        .param("namingConvention", "camelCase"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMetadata));
    }

    @Test
    void testGetMetadataString_withFile_shouldReturnMetadataJSON() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        String expectedMetadata = "{\"@context\": \"http://www.w3.org/ns/csvw\"}";
        
        // Create real AppConfig
        AppConfig config = new AppConfig.Builder("src/test/resources/RDFFeatures/basic_triple.nt")
                .multipleTables(false)  // ONE table
                .parsing("RDF4J")
                .firstNormalForm(true)
                .preferredLanguages("en,cs,de")
                .columnNamingConvention("SCREAMING_SNAKE_CASE")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getMetadataStringFromFile to return expected metadata
        when(rdFtoCSVWService.getMetadataStringFromFile(any(AppConfig.class)))
                .thenReturn(expectedMetadata);

        mockMvc.perform(multipart("/metadata/string")
                        .file(file)
                        .param("table", "ONE")
                        .param("conversionMethod", "RDF4J")
                        .param("firstNormalForm", "true")
                        .param("preferredLanguages", "en,cs,de")
                        .param("namingConvention", "SCREAMING_SNAKE_CASE"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMetadata));
    }

    @Test
    void testGetMetadataFile_withUrl_shouldReturnMetadataFile() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        byte[] expectedMetadataBytes = "{\"@context\": \"http://www.w3.org/ns/csvw\"}".getBytes(StandardCharsets.UTF_8);
        
        // Create real AppConfig
        AppConfig config = new AppConfig.Builder(fakeURL)
                .multipleTables(false)  // ONE table
                .parsing("RDF4J")
                .firstNormalForm(false)
                .columnNamingConvention("dot.notation")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getMetadataFileFromURL to return expected metadata bytes
        when(rdFtoCSVWService.getMetadataFileFromURL(any(AppConfig.class)))
                .thenReturn(expectedMetadataBytes);

        mockMvc.perform(get("/metadata")
                        .param("url", fakeURL)
                        .param("table", "ONE")
                        .param("conversionMethod", "RDF4J")
                        .param("firstNormalForm", "false")
                        .param("namingConvention", "dot.notation"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(expectedMetadataBytes));
    }

    @Test
    void testGetMetadataFile_withFile_shouldReturnMetadataFile() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        byte[] expectedMetadataBytes = "{\"@context\": \"http://www.w3.org/ns/csvw\"}".getBytes(StandardCharsets.UTF_8);
        
        // Create real AppConfig
        AppConfig config = new AppConfig.Builder("src/test/resources/RDFFeatures/basic_triple.nt")
                .multipleTables(true)  // MORE tables
                .parsing("BIGFILESTREAMING")
                .firstNormalForm(true)
                .preferredLanguages("cs")
                .columnNamingConvention("original")
                .build();
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), anyBoolean(), anyString(), anyString()))
                .thenReturn(config);
        
        // Mock getMetadataFileFromFile to return expected metadata bytes
        when(rdFtoCSVWService.getMetadataFileFromFile(any(AppConfig.class)))
                .thenReturn(expectedMetadataBytes);

        mockMvc.perform(multipart("/metadata")
                        .file(file)
                        .param("table", "MORE")
                        .param("conversionMethod", "BIGFILESTREAMING")
                        .param("firstNormalForm", "true")
                        .param("preferredLanguages", "cs")
                        .param("namingConvention", "original"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(expectedMetadataBytes));
    }
}
