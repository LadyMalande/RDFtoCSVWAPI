package org.rdftocsvconverter.RDFtoCSVW;

import com.miklosova.rdftocsvw.support.AppConfig;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;
import org.rdftocsvconverter.RDFtoCSVW.model.ComputationTask;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.rdftocsvconverter.RDFtoCSVW.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.rdftocsvconverter.RDFtoCSVW.api.controller.RDFtoCSVWController;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(controllers = RDFtoCSVWController.class)
class RDFtoCSVWControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RDFtoCSVWService rdFtoCSVWService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Test
    void testGetCSVW_withFileUpload_shouldReturnZip() throws Exception {
        // Load test file from resources
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.ttl", "text/turtle", testFileBytes);

        // Mock service to return dummy ZIP bytes
        byte[] fakeZipContent = "dummy zip content".getBytes(StandardCharsets.UTF_8);
        AppConfig mockConfig = new AppConfig.Builder("test.ttl").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(mockConfig);
        when(rdFtoCSVWService.getZipFile(any(AppConfig.class)))
                .thenReturn(fakeZipContent);

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
        AppConfig mockConfig = new AppConfig.Builder(fakeURL).build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(mockConfig);
        when(rdFtoCSVWService.getZipFile(any(AppConfig.class)))
                .thenReturn(fakeZipContent);

        mockMvc.perform(post("/rdftocsvw")
                        //.file(mockFile)
                        .param("fileURL", fakeURL)
                        .param("choice", "RDF4J")
                        .param("tables", "ONE")
                        .param("firstNormalForm", "true"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fakeZipContent));
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
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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
        
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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
        
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
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

    // ========== NEW TESTS FOR IMPROVED COVERAGE ==========

    @Test
    void testSanityCheck() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello from RDFtoCSV"));
    }

    @Test
    void testCheckRedisHealth_Success() throws Exception {
        // Mock Redis connection
        RedisConnectionFactory connectionFactory = org.mockito.Mockito.mock(RedisConnectionFactory.class);
        RedisConnection connection = org.mockito.Mockito.mock(RedisConnection.class);
        
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        mockMvc.perform(get("/health/redis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Redis is connected and accessible"));
    }

    @Test
    void testCheckRedisHealth_ConnectionFailure() throws Exception {
        // Mock Redis connection failure
        RedisConnectionFactory connectionFactory = org.mockito.Mockito.mock(RedisConnectionFactory.class);
        
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenThrow(new RedisConnectionFailureException("Cannot connect"));

        mockMvc.perform(get("/health/redis"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.message").value("Cannot connect to Redis"));
    }

    @Test
    void testCheckRedisHealth_UnexpectedError() throws Exception {
        // Mock unexpected error
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/health/redis"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Error checking Redis connection"));
    }

    @Test
    void testDebugRedisConfig_Success() throws Exception {
        // Mock Redis connection factory
        RedisConnectionFactory connectionFactory = org.mockito.Mockito.mock(RedisConnectionFactory.class);
        RedisConnection connection = org.mockito.Mockito.mock(RedisConnection.class);
        
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.toString()).thenReturn("RedisConnection[localhost:6379]");

        mockMvc.perform(get("/debug/redis-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redisHost").exists())
                .andExpect(jsonPath("$.redisPort").exists())
                .andExpect(jsonPath("$.connectionFactory").exists());
    }

    @Test
    void testDebugRedisConfig_WithError() throws Exception {
        // Mock error when getting connection
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Config error"));

        mockMvc.perform(get("/debug/redis-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Config error"));
    }

    @Test
    void testConvertRDFToCSVWAsync_WithFile_Success() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        String sessionId = "test-session-123";
        AppConfig mockConfig = new AppConfig.Builder("test.nt").build();
        
        when(taskService.createTask()).thenReturn(sessionId);
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(mockConfig);
        // computeAsyncAndStore returns void, so no need to mock the return value

        mockMvc.perform(multipart("/rdftocsvw/async")
                        .file(file)
                        .param("conversionMethod", "RDF4J")
                        .param("table", "ONE")
                        .param("firstNormalForm", "false"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sessionId").value(sessionId));
    }

    @Test
    void testConvertRDFToCSVWAsync_WithURL_Success() throws Exception {
        String testURL = "https://example.org/test.ttl";
        String sessionId = "test-session-456";
        AppConfig mockConfig = new AppConfig.Builder(testURL).build();
        
        when(taskService.createTask()).thenReturn(sessionId);
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(mockConfig);
        // computeAsyncAndStore returns void, so no need to mock the return value

        mockMvc.perform(post("/rdftocsvw/async")
                        .param("fileURL", testURL)
                        .param("conversionMethod", "STREAMING")
                        .param("table", "MORE")
                        .param("firstNormalForm", "true"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sessionId").value(sessionId));
    }

    @Test
    void testConvertRDFToCSVWAsync_NoFileOrURL_BadRequest() throws Exception {
        mockMvc.perform(post("/rdftocsvw/async")
                        .param("conversionMethod", "RDF4J"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Either file or fileURL must be provided"));
    }

    @Test
    void testConvertRDFToCSVWAsync_RedisConnectionFailure() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        when(taskService.createTask()).thenThrow(new RedisConnectionFailureException("Redis unavailable"));

        mockMvc.perform(multipart("/rdftocsvw/async")
                        .file(file))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Redis")));
    }

    @Test
    void testGetComputationStatus_SessionNotFound() throws Exception {
        String sessionId = "nonexistent-session";
        
        when(taskService.getTask(sessionId)).thenReturn(null);

        mockMvc.perform(get("/status/" + sessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Session not found"))
                .andExpect(jsonPath("$.sessionId").value(sessionId));
    }

    @Test
    void testGetComputationStatus_Computing() throws Exception {
        String sessionId = "computing-session";
        ComputationTask task = new ComputationTask(sessionId);
        task.setStatus(ComputationStatus.COMPUTING);
        
        when(taskService.getTask(sessionId)).thenReturn(task);

        mockMvc.perform(get("/status/" + sessionId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("COMPUTING"))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.message").value("Computation is still in progress"));
    }

    @Test
    void testGetComputationStatus_Done() throws Exception {
        String sessionId = "done-session";
        byte[] resultData = "ZIP content".getBytes(StandardCharsets.UTF_8);
        
        ComputationTask task = new ComputationTask(sessionId);
        task.setStatus(ComputationStatus.DONE);
        task.setResult(resultData);
        
        when(taskService.getTask(sessionId)).thenReturn(task);

        mockMvc.perform(get("/status/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=csvw-output.zip"))
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(resultData));
    }

    @Test
    void testGetComputationStatus_Failed() throws Exception {
        String sessionId = "failed-session";
        
        ComputationTask task = new ComputationTask(sessionId);
        task.setStatus(ComputationStatus.FAILED);
        task.setErrorMessage("Conversion failed due to invalid RDF");
        
        when(taskService.getTask(sessionId)).thenReturn(task);

        mockMvc.perform(get("/status/" + sessionId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.error").value("Conversion failed due to invalid RDF"));
    }

    @Test
    void testGetCSVW_MissingBothFileAndURL() throws Exception {
        mockMvc.perform(post("/rdftocsvw")
                        .param("conversionMethod", "RDF4J"))
                .andExpect(status().is5xxServerError()); // Expecting error when both file and URL are missing
    }

    @Test
    void testGetCSVW_WithIOException() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.ttl", "text/turtle", testFileBytes);

        AppConfig mockConfig = new AppConfig.Builder("test.ttl").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(mockConfig);
        when(rdFtoCSVWService.getZipFile(any(AppConfig.class)))
                .thenThrow(new IOException("File processing error"));

        mockMvc.perform(multipart("/rdftocsvw")
                        .file(file)
                        .param("choice", "RDF4J"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testConvertRDFToCSV_GetWithIOException() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        
        AppConfig config = new AppConfig.Builder(fakeURL).build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getCSVString(any(AppConfig.class)))
                .thenThrow(new IOException("Network error"));

        mockMvc.perform(get("/csv/string")
                        .param("url", fakeURL))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("There has been a problem with parsing your request"));
    }

    @Test
    void testConvertRDFToCSV_PostWithIOException() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        AppConfig config = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getCSVStringFromFile(any(AppConfig.class)))
                .thenThrow(new IOException("File read error"));

        mockMvc.perform(multipart("/csv/string")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("There has been a problem with parsing your request"));
    }

    @Test
    void testConvertRDFToCSVFile_PostWithIOException() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        AppConfig config = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getCSVFileFromFile(any(AppConfig.class)))
                .thenThrow(new IOException("Conversion error"));

        mockMvc.perform(multipart("/csv")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertRDFToCSVFile_GetWithIOException() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        
        AppConfig config = new AppConfig.Builder(fakeURL).build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getCSVFileFromURL(any(AppConfig.class)))
                .thenThrow(new IOException("Download error"));

        mockMvc.perform(get("/csv")
                        .param("url", fakeURL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertRDFToCSVWMetadataFile_PostWithIOException() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        AppConfig config = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getMetadataFileFromFile(any(AppConfig.class)))
                .thenThrow(new IOException("Metadata generation error"));

        mockMvc.perform(multipart("/metadata")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertRDFToCSVWMetadataFile_GetWithIOException() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        
        AppConfig config = new AppConfig.Builder(fakeURL).build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getMetadataFileFromURL(any(AppConfig.class)))
                .thenThrow(new IOException("Metadata download error"));

        mockMvc.perform(get("/metadata")
                        .param("url", fakeURL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertRDFToCSVWMetadata_GetWithIOException() throws Exception {
        String fakeURL = "https://example.org/test.ttl";
        
        AppConfig config = new AppConfig.Builder(fakeURL).build();
        when(rdFtoCSVWService.buildAppConfig(anyString(), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getMetadataString(any(AppConfig.class)))
                .thenThrow(new IOException("String conversion error"));

        mockMvc.perform(get("/metadata/string")
                        .param("url", fakeURL))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("There has been a problem with parsing your request"));
    }

    @Test
    void testConvertRDFToCSVWMetadata_PostWithIOException() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        AppConfig config = new AppConfig.Builder("test.nt").build();
        when(rdFtoCSVWService.buildAppConfig(any(org.springframework.web.multipart.MultipartFile.class), anyString(), anyString(), any(Boolean.class), nullable(String.class), nullable(String.class)))
                .thenReturn(config);
        when(rdFtoCSVWService.getMetadataStringFromFile(any(AppConfig.class)))
                .thenThrow(new IOException("Metadata string error"));

        mockMvc.perform(multipart("/metadata/string")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("There has been a problem with parsing your request"));
    }

    @Test
    void testConvertRDFToCSVWAsync_GenericException_WithRedisInMessage() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        when(taskService.createTask()).thenThrow(new RuntimeException("Failed to connect to Redis server"));

        mockMvc.perform(multipart("/rdftocsvw/async")
                        .file(file))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Unable to connect to Redis. Please ensure Redis is running."));
    }

    @Test
    void testConvertRDFToCSVWAsync_GenericException_WithoutRedisInMessage() throws Exception {
        byte[] testFileBytes = Files.readAllBytes(Path.of("src/test/resources/RDFFeatures/basic_triple.nt"));
        MockMultipartFile file = new MockMultipartFile("file", "test.nt", "application/n-triples", testFileBytes);
        
        when(taskService.createTask()).thenThrow(new RuntimeException("Configuration error"));

        mockMvc.perform(multipart("/rdftocsvw/async")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Configuration error"));
    }

    @Test
    @Disabled
    void testGetComputationStatus_UnknownStatus() throws Exception {
        String sessionId = "unknown-status-session";
        
        // Create a task with a status that doesn't match any case (simulate by using reflection or a custom mock)
        ComputationTask task = new ComputationTask(sessionId);
        // We need to set a status that's not DONE, COMPUTING, or FAILED
        // Since ComputationStatus is an enum, we'll need to check what values exist
        // For now, let's assume there's a way to trigger the default case
        // We can use reflection to set an unusual state or mock the enum behavior
        // However, since we can't easily add a new enum value, we'll use a workaround:
        // We'll create a custom mock that returns null for getStatus()
        ComputationTask mockTask = org.mockito.Mockito.mock(ComputationTask.class);
        when(mockTask.getStatus()).thenReturn(null); // This will trigger the default case
        
        when(taskService.getTask(sessionId)).thenReturn(mockTask);

        mockMvc.perform(get("/status/" + sessionId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Unknown status"))
                .andExpect(jsonPath("$.sessionId").value(sessionId));
    }
}
