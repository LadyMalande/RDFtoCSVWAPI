package org.rdftocsvconverter.RDFtoCSVW.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rdftocsvconverter.RDFtoCSVW.BaseTest;
import org.rdftocsvconverter.RDFtoCSVW.enums.ParsingChoice;
import org.rdftocsvconverter.RDFtoCSVW.enums.TableChoice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


/**
 * The RDFtoCSVW service test. Tests methods under the API that communicate with the library RDFtoCSV.
 */
class RDFtoCSVWServiceTest extends BaseTest {

    private RDFtoCSVWService rdfToCSVWService;

    @Mock
    private MultipartFile mockMultipartFile;

    /**
     * Sets up mocks and the service.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rdfToCSVWService = new RDFtoCSVWService();
    }

    /**
     * Test get CSVW with multipart file.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetCSVW_WithMultipartFile}, hash: 8AAEC14C2EB85511FCEBFEA9FBDB0105
    @Test
    void testGetCSVW_WithMultipartFile() throws IOException {
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContents.getBytes()));
        when(mockMultipartFile.getOriginalFilename()).thenReturn("simpsons.ttl");
        byte[] result = rdfToCSVWService.getCSVW(mockMultipartFile, "", "RDF4J", "ONE", true);
        assertNotNull(result);
    }

    /**
     * Test get CSVW with file url.
     *
     * @throws IOException the IO exception
     */
    @Test
    void testGetCSVW_WithFileURL() throws IOException {
        byte[] result = rdfToCSVWService.getCSVW(null, "https://w3c.github.io/csvw/tests/test005.ttl", "RDF4J", "ONE", true);
        assertNotNull(result);
    }

    /**
     * Test save file method.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testSaveFile}, hash: F28BEED4C998047E39E13DFF8F76F45B
    @Test
    void testSaveFile() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons5.ttl", "text/plain", fileContents.getBytes());

        File savedFile = rdfToCSVWService.saveFile(mockMultipartFile);
        assertNotNull(savedFile);
        assertTrue(savedFile.exists());
        boolean delete = savedFile.delete();
        System.out.println("testSaveFile case: File deleted at the end? " + delete);
    }

    /**
     * Test get csv string from the service.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetCSVString}, hash: 7B7AA477011631A3D581B6E8E7E43250
    @Test
    void testGetCSVString() throws IOException {
        String result = rdfToCSVWService.getCSVString("https://w3c.github.io/csvw/tests/test005.ttl", new HashMap<>());
        assertNotNull(result);
    }

    /**
     * Test get csv string from RDF file.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetCSVStringFromFile}, hash: F455A9734120431157B5677FDEC67C1A
    @Test
    void testGetCSVStringFromFile() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons6.ttl", "text/plain", fileContents.getBytes());
        String result = rdfToCSVWService.getCSVStringFromFile(mockMultipartFile, new HashMap<>());
        assertNotNull(result);
    }

    /**
     * Test get metadata string.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetMetadataString}, hash: B6DDDD4754C3B7EC0B4B8C48303A82A2
    @Test
    void testGetMetadataString() throws IOException {
        String result = rdfToCSVWService.getMetadataString("https://w3c.github.io/csvw/tests/test005.ttl", new HashMap<>());
        assertNotNull(result);
    }

    /**
     * Test get metadata string from RDF file.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetMetadataStringFromFile}, hash: D94C3911B33049F18DF5EC409E5A900F
    @Test
    void testGetMetadataStringFromFile() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons7.ttl", "text/plain", fileContents.getBytes());

        String result = rdfToCSVWService.getMetadataStringFromFile(mockMultipartFile, new HashMap<>());
        assertNotNull(result);
    }

    /**
     * Test prepare config parameter.
     *
     * @param table            the table
     * @param conversionMethod the conversion method
     * @param firstNormalForm  the first normal form
     */
//BaseRock generated method id: ${testPrepareConfigParameter}, hash: FE0CDBCBF7950EBE1AB4EA6A46495AE1
    @ParameterizedTest
    @CsvSource({"ONE,RDF4J,true", "TWO,JENA,false", ",,"})
    void testPrepareConfigParameter(String table, String conversionMethod, Boolean firstNormalForm) {
        Map<String, String> result = rdfToCSVWService.prepareConfigParameter(table, conversionMethod, firstNormalForm);
        assertNotNull(result);
        assertEquals(table != null ? table : TableChoice.ONE.toString(), result.get("table"));
        assertEquals(conversionMethod != null ? conversionMethod : ParsingChoice.RDF4J.toString(), result.get("readMethod"));
        assertEquals(String.valueOf(firstNormalForm != null ? firstNormalForm : false), result.get("firstNormalForm"));
    }

    /**
     * Test get csv file from RDF url.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetCSVFileFromURL}, hash: 16F196FBA61BEE393113A7982C4BC6EC
    @Test
    void testGetCSVFileFromURL() throws IOException {
        byte[] result = rdfToCSVWService.getCSVFileFromURL(
                "https://w3c.github.io/csvw/tests/test005.ttl", new HashMap<>());
        //"https://raw.githubusercontent.com/LadyMalande/RDFtoCSV/refs/heads/main/src/test/resources/differentSerializations/testingInput.rdf", new HashMap<>());
        assertNotNull(result);
    }

    /**
     * Test get CSV file from RDF file.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetCSVFileFromFile}, hash: D727BAC0885F82ECDD192455027D5852
    @Test
    void testGetCSVFileFromFile() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons8.ttl", "text/plain", fileContents.getBytes());
        byte[] result = rdfToCSVWService.getCSVFileFromFile(mockMultipartFile, new HashMap<>());
        assertNotNull(result);
    }

    /**
     * Test get metadata file from RDF url.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetMetadataFileFromURL}, hash: 629543793994451CC24B0BEF8F642FB8
    @Test
    void testGetMetadataFileFromURL() throws IOException {
        byte[] result = rdfToCSVWService.getMetadataFileFromURL("https://w3c.github.io/csvw/tests/test005.ttl", configCreator(null, null, null));
        assertNotNull(result);
    }

    /**
     * Test get metadata file from RDF file.
     *
     * @throws IOException the IO exception
     */
//BaseRock generated method id: ${testGetMetadataFileFromFile}, hash: D19F926938BD2E238BFFBF4B6B604AD6
    @Test
    void testGetMetadataFileFromFile() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons9.ttl", "text/plain", fileContents.getBytes());

        byte[] result = rdfToCSVWService.getMetadataFileFromFile(mockMultipartFile, new HashMap<>());
        assertNotNull(result);
    }

    /**
     * Test get csvw from RDF file.
     *
     * @throws IOException the IO exception
     */
    @Test
    void testGetCSVWFromFile() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons10.ttl", "text/plain", fileContents.getBytes());

        byte[] result = rdfToCSVWService.getCSVW(mockMultipartFile, null, String.valueOf(ParsingChoice.RDF4J), String.valueOf(TableChoice.ONE), true);
        assertNotNull(result);
    }

    /**
     * Test get csvw from RDF file and url.
     *
     * @throws IOException the IO exception
     */
    @Test
    void testGetCSVWFromFileAndURL() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons11.ttl", "text/plain", fileContents.getBytes());

        byte[] result = rdfToCSVWService.getCSVW(mockMultipartFile, "https://w3c.github.io/csvw/tests/test005.ttl", String.valueOf(ParsingChoice.RDF4J), String.valueOf(TableChoice.ONE), true);
        assertNotNull(result);
    }

    /**
     * Test get csvw from RDF file and empty url.
     *
     * @throws IOException the IO exception
     */
    @Test
    void testGetCSVWFromFileAndEmptyURL() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "simpsons3.ttl", "text/plain", fileContents.getBytes());

        byte[] result = rdfToCSVWService.getCSVW(mockMultipartFile, "", String.valueOf(ParsingChoice.RDF4J), String.valueOf(TableChoice.ONE), true);
        assertNotNull(result);
    }

    /**
     * Test get csvw from RDF url.
     *
     * @throws IOException the IO exception
     */
    @Test
    void testGetCSVWFromURL() throws IOException {

        byte[] result = rdfToCSVWService.getCSVW(null, "https://w3c.github.io/csvw/tests/test005.ttl", String.valueOf(ParsingChoice.RDF4J), String.valueOf(TableChoice.ONE), true);
        assertNotNull(result);
    }

}