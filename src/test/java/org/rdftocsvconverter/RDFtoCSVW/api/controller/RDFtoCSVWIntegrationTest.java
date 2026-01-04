package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.rdftocsvconverter.RDFtoCSVW.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.rdftocsvconverter.RDFtoCSVW.RDFToCSVWApiApplication;

@SpringBootTest(classes = RDFToCSVWApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RDFtoCSVWIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private final Path inputDir = Paths.get("src/test/resources/RDFFeatures");
    private final Path expectedDir = Paths.get("src/test/resources/integration/expected");

    private void assertZipContentEquals(byte[] actualZipBytes, Path expectedZipPath) throws IOException {
        try (
                ZipInputStream actualZip = new ZipInputStream(new ByteArrayInputStream(actualZipBytes));
                ZipInputStream expectedZip = new ZipInputStream(Files.newInputStream(expectedZipPath))
        ) {
            Map<String, String> actualEntries = new HashMap<>();
            ZipEntry entry;
            while ((entry = actualZip.getNextEntry()) != null) {
                actualEntries.put(entry.getName(), new String(actualZip.readAllBytes(), StandardCharsets.UTF_8));
            }

            while ((entry = expectedZip.getNextEntry()) != null) {
                String expectedContent = new String(expectedZip.readAllBytes(), StandardCharsets.UTF_8);
                String actualContent = actualEntries.get(entry.getName());
                assertNotNull(actualContent, "Missing file in actual ZIP: " + entry.getName());
                assertEquals(expectedContent.trim(), actualContent.trim(), "Content mismatch in: " + entry.getName());
            }
        }
    }


    static Stream<Arguments> provideRdfTestFiles() throws IOException {
        Path inputDir = Paths.get("src/test/resources/RDFFeatures");
        return Files.list(inputDir)
                .filter(Files::isRegularFile)
                .map(path -> Arguments.of(path.getFileName().toString()));
    }

    @ParameterizedTest(name = "Test RDF to CSVW with file: {0}")
    @MethodSource("provideRdfTestFiles")
    void testCSVWConversion_withFile(String fileName) throws Exception {
        Path inputFile = inputDir.resolve(fileName);
        Path expectedZip = expectedDir.resolve(fileName.replaceFirst("\\..+$", ".zip")); // replace .ttl/.rdf with .zip

        // Skip test if expected file doesn't exist
        if (!Files.exists(expectedZip)) {
            System.out.println("Skipping test for " + fileName + " - expected file not found: " + expectedZip);
            return;
        }

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", fileName, "text/turtle", Files.readAllBytes(inputFile)
        );

        MvcResult result = mockMvc.perform(multipart("/rdftocsvw")
                        .file(mockFile)
                        .param("choice", "RDF4J") // Adjust if you need dynamic mapping
                        .param("tables", "ONE")
                        .param("firstNormalForm", "true"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] actualZip = result.getResponse().getContentAsByteArray();
        assertZipContentEquals(actualZip, expectedZip);
    }

    @Test
    void testCSVWConversion_withMultipartFile() throws Exception {
        String testFileName = "basic_triple.ttl";
        Path inputFile = inputDir.resolve(testFileName);
        Path expectedZip = expectedDir.resolve("example.zip");

        // Skip test if expected file doesn't exist
        if (!Files.exists(expectedZip)) {
            System.out.println("Skipping testCSVWConversion_withMultipartFile - expected file not found: " + expectedZip);
            return;
        }

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", testFileName, "text/turtle", Files.readAllBytes(inputFile)
        );

        MvcResult result = mockMvc.perform(multipart("/rdftocsvw")
                        .file(mockFile)
                        .param("choice", "RDF4J")
                        .param("tables", "ONE")
                        .param("firstNormalForm", "true"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] actualZip = result.getResponse().getContentAsByteArray();
        assertZipContentEquals(actualZip, expectedZip);
    }


}
