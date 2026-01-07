package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import com.miklosova.rdftocsvw.support.AppConfig;
import org.rdftocsvconverter.RDFtoCSVW.enums.ParsingChoice;
import org.rdftocsvconverter.RDFtoCSVW.enums.TableChoice;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.rdftocsvconverter.RDFtoCSVW.service.TaskService;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;

/**
 * Test that the controller uses the OPEN API correctly to be able to respond to requests.
 */
@WebMvcTest(controllers = RDFtoCSVWController.class)
class RDFtoCSVWControllerTest {

    private final String testContent = """
                <http://example.org/foo> <http://example.org/bar> _:v .
                _:v <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> _:c .
                _:c <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Datatype> .""";
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RDFtoCSVWService rdFtoCSVWService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //BaseRock generated method id: ${testSanityCheck}, hash: 292A66BD6E1586966CAF2CB0FA9783AD
    @Test
    void testSanityCheck() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/")).andExpect(status().isOk()).andExpect(content().string("Hello from RDFtoCSV"));
    }

    //BaseRock generated method id: ${testGetCSVW}, hash: 9F307BA08DE0C68BD8135114398D3E56
    @Test
    void testGetCSVW() throws Exception {
        byte[] mockZipContent = "Mock CSVW content".getBytes();
        AppConfig mockConfig = new AppConfig.Builder("test.nt").build();
        
        // Mock the actual methods the controller calls - using nullable for optional params
        when(rdFtoCSVWService.buildAppConfig(
                any(MultipartFile.class), 
                anyString(), 
                anyString(), 
                any(Boolean.class), 
                nullable(String.class), 
                nullable(String.class)))
                .thenReturn(mockConfig);
        when(rdFtoCSVWService.getZipFile(any(AppConfig.class)))
                .thenReturn(mockZipContent);
        
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "text/plain", """
                <http://example.org/foo> <http://example.org/bar> _:v .
                _:v <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> _:c .
                _:c <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Datatype> .""".getBytes());
        
        mockMvc.perform(MockMvcRequestBuilders.multipart("/rdftocsvw")
                        .file(file)
                        .param("conversionMethod", String.valueOf(ParsingChoice.RDF4J))
                        .param("table", String.valueOf(TableChoice.ONE))
                        .param("firstNormalForm", "true"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mockZipContent));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVGet}, hash: 75C58B6B5281A0697658D81B67FFFB6B
    @Test
    void testConvertRDFToCSVGet() throws Exception {
        String mockCSVString = "id,name\n1,John";
        AppConfig mockConfig = new AppConfig.Builder("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getCSVString(any(AppConfig.class))).thenReturn(mockCSVString);
        mockMvc.perform(MockMvcRequestBuilders.get("/csv/string").param("url", "https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").param("table", String.valueOf(TableChoice.ONE)).param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().string(mockCSVString));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVPost}, hash: F2314CA703A6895CF4529421965E069A
    @Test
    void testConvertRDFToCSVPost() throws Exception {
        String mockCSVString = "id,name\n1,John";
        AppConfig mockConfig = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getCSVStringFromFile(any(AppConfig.class))).thenReturn(mockCSVString);
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "text/plain", testContent.getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/csv/string").file(file).param("table", String.valueOf(TableChoice.ONE)).param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().string(mockCSVString));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVFilePost}, hash: 197E9261959974E89336B6B9E4A18661
    @Test
    void testConvertRDFToCSVFilePost() throws Exception {
        byte[] mockCSVFile = "id,name\n1,John".getBytes();
        AppConfig mockConfig = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getCSVFileFromFile(any(AppConfig.class))).thenReturn(mockCSVFile);
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "text/plain", testContent.getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/csv").file(file).param("table", String.valueOf(TableChoice.MORE)).param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().bytes(mockCSVFile));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVFileGet}, hash: 2611A2683ACF77C51799CCCEFCD6CE1B
    @Test
    void testConvertRDFToCSVFileGet() throws Exception {
        byte[] mockCSVFile = "id,name\n1,John".getBytes();
        AppConfig mockConfig = new AppConfig.Builder("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getCSVFileFromURL(any(AppConfig.class))).thenReturn(mockCSVFile);
        mockMvc.perform(MockMvcRequestBuilders.get("/csv").param("url", "https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").param("table", String.valueOf(TableChoice.MORE)).param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().bytes(mockCSVFile));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVWMetadataFilePost}, hash: 6F7E96FE2CA6EF6AC0B8E4C352BC400B
    @Test
    void testConvertRDFToCSVWMetadataFilePost() throws Exception {
        byte[] mockMetadataFile = "{\"@context\": \"http://www.w3.org/ns/csvw\"}".getBytes();
        AppConfig mockConfig = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getMetadataFileFromFile(any(AppConfig.class))).thenReturn(mockMetadataFile);
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "text/plain", testContent.getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/metadata").file(file).param("table", String.valueOf(TableChoice.MORE)).param("conversionMethod", String.valueOf(ParsingChoice.STREAMING) ).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().bytes(mockMetadataFile));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVWMetadataFileGet}, hash: 940B107FD1C7F89D7E713F64DC908897
    @Test
    void testConvertRDFToCSVWMetadataFileGet() throws Exception {
        byte[] mockMetadataFile = "{\"@context\": \"http://www.w3.org/ns/csvw\"}".getBytes();
        AppConfig mockConfig = new AppConfig.Builder("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getMetadataFileFromURL(any(AppConfig.class))).thenReturn(mockMetadataFile);
        mockMvc.perform(MockMvcRequestBuilders.get("/metadata").param("url", "https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").param("table", String.valueOf(TableChoice.ONE)).param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().bytes(mockMetadataFile));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVWMetadataStringGet}, hash: 2536297B769D98AEF535943BED03C643
    @Test
    void testConvertRDFToCSVWMetadataStringGet() throws Exception {
        String mockMetadataString = "{\"@context\": \"http://www.w3.org/ns/csvw\"}";
        AppConfig mockConfig = new AppConfig.Builder("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getMetadataString(any(AppConfig.class))).thenReturn(mockMetadataString);
        mockMvc.perform(MockMvcRequestBuilders.get("/metadata/string").param("url", "https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/examples/MissingDataExample.ttl").param("table", String.valueOf(TableChoice.ONE)).param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().string(mockMetadataString));
    }

    //BaseRock generated method id: ${testConvertRDFToCSVWMetadataStringPost}, hash: 9AE1CDA7709B183B461D89C9BAE81481
    @Test
    void testConvertRDFToCSVWMetadataStringPost() throws Exception {
        String mockMetadataString = "{\"@context\": \"http://www.w3.org/ns/csvw\"}";
        AppConfig mockConfig = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getMetadataStringFromFile(any(AppConfig.class))).thenReturn(mockMetadataString);
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "text/plain", testContent.getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/metadata/string").file(file).param("table", String.valueOf(TableChoice.ONE)).param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().isOk()).andExpect(content().string(mockMetadataString));
    }

    @Test
    void testConvertRDFToCSVWMetadataStringPostNOK() throws Exception {
        String mockMetadataString = "{\"@context\": \"http://www.w3.org/ns/csvw\"}";
        AppConfig mockConfig = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(), nullable(String.class), nullable(String.class))).thenReturn(mockConfig);
        when(rdFtoCSVWService.getMetadataStringFromFile(any(AppConfig.class))).thenReturn(mockMetadataString);
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "text/plain", testContent.getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/metadata/string").file(file).param("table", "badParam").param("conversionMethod", String.valueOf(ParsingChoice.RDF4J)).param("firstNormalForm", "true")).andExpect(status().is4xxClientError()).andExpect(content().string("{\"error\":\"Invalid parameter\",\"message\":\"Parameter 'table' should be of type TableChoice. Provided value: 'badParam'.\"}"));
    }
}
