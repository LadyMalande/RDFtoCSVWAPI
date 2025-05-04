package org.rdftocsvconverter.RDFtoCSVW;

import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.api.controller.RDFtoCSVWController;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
}
