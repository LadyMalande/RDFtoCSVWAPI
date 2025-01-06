package org.rdftocsvconverter.RDFtoCSVW.service;

 import org.junit.jupiter.api.Disabled;
 import org.rdftocsvconverter.RDFtoCSVW.enums.ParsingChoice;

 import java.lang.reflect.Method;

 import org.junit.jupiter.api.Test;
 import java.nio.file.*;
 import java.io.File;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.junit.jupiter.api.io.TempDir;

 import java.io.*;

 import org.junit.jupiter.params.provider.CsvSource;
 import static org.mockito.Mockito.*;
 import java.io.IOException;
 import java.util.HashMap;
 import org.springframework.web.multipart.MultipartFile;
 import org.junit.jupiter.api.BeforeEach;
 import java.util.Map;
 import java.nio.file.Path;
 import org.junit.jupiter.params.ParameterizedTest;
 import org.rdftocsvconverter.RDFtoCSVW.enums.TableChoice;
 import static org.junit.jupiter.api.Assertions.*;
 import com.miklosova.rdftocsvw.converter.RDFtoCSV;

 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

 @Disabled
class RDFtoCSVWServiceTest {

     private RDFtoCSVWService rdfToCSVWService;

     @Mock
     private MultipartFile mockMultipartFile;

     @Mock
     private RDFtoCSV mockRDFtoCSV;

     @TempDir
     Path tempDir;

     @BeforeEach
     void setUp() {
         MockitoAnnotations.openMocks(this);
         rdfToCSVWService = new RDFtoCSVWService();
     }

     //BaseRock generated method id: ${testGetCSVW_WithMultipartFile}, hash: 8AAEC14C2EB85511FCEBFEA9FBDB0105
     @Test
     void testGetCSVW_WithMultipartFile() throws IOException {
         when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
         when(mockMultipartFile.getOriginalFilename()).thenReturn("test.nt");
         byte[] result = rdfToCSVWService.getCSVW(mockMultipartFile, "", "RDF4J", "ONE", true);
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testGetCSVW_WithFileURL}, hash: B179926BC3A4B0C8A01CE4A55CFFE635
     @Test
     void testGetCSVW_WithFileURL() throws IOException {
         byte[] result = rdfToCSVWService.getCSVW(null, "http://example.com/test.nt", "RDF4J", "ONE", true);
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testSaveFile}, hash: F28BEED4C998047E39E13DFF8F76F45B
     @Test
     void testSaveFile() throws IOException {
         when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
         when(mockMultipartFile.getOriginalFilename()).thenReturn("test.nt");
         File savedFile = rdfToCSVWService.saveFile(mockMultipartFile);
         assertNotNull(savedFile);
         assertTrue(savedFile.exists());
         savedFile.delete();
     }

     //BaseRock generated method id: ${testGetCSVString}, hash: 7B7AA477011631A3D581B6E8E7E43250
     @Test
     void testGetCSVString() throws IOException {
         String result = rdfToCSVWService.getCSVString("https://raw.githubusercontent.com/LadyMalande/RDFtoCSV/refs/heads/main/idOutuput.ttl", new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testGetCSVStringFromFile}, hash: F455A9734120431157B5677FDEC67C1A
     @Test
     void testGetCSVStringFromFile() throws IOException {
         when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
         when(mockMultipartFile.getOriginalFilename()).thenReturn("test.nt");
         String result = rdfToCSVWService.getCSVStringFromFile(mockMultipartFile, new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testGetMetadataString}, hash: B6DDDD4754C3B7EC0B4B8C48303A82A2
     @Test
     void testGetMetadataString() throws IOException {
         String result = rdfToCSVWService.getMetadataString("https://raw.githubusercontent.com/LadyMalande/RDFtoCSV/refs/heads/main/idOutuput.ttl", new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testGetMetadataStringFromFile}, hash: D94C3911B33049F18DF5EC409E5A900F
     @Test
     void testGetMetadataStringFromFile() throws IOException {
         when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
         when(mockMultipartFile.getOriginalFilename()).thenReturn("test.nt");
         String result = rdfToCSVWService.getMetadataStringFromFile(mockMultipartFile, new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testPrepareConfigParameter}, hash: FE0CDBCBF7950EBE1AB4EA6A46495AE1
     @ParameterizedTest
     @CsvSource({ "ONE,RDF4J,true", "TWO,JENA,false", ",," })
     void testPrepareConfigParameter(String table, String conversionMethod, Boolean firstNormalForm) {
         Map<String, String> result = rdfToCSVWService.prepareConfigParameter(table, conversionMethod, firstNormalForm);
         assertNotNull(result);
         assertEquals(table != null ? table : TableChoice.ONE.toString(), result.get("table"));
         assertEquals(conversionMethod != null ? conversionMethod : ParsingChoice.RDF4J.toString(), result.get("readMethod"));
         assertEquals(String.valueOf(firstNormalForm != null ? firstNormalForm : false), result.get("firstNormalForm"));
     }

     //BaseRock generated method id: ${testGetCSVFileFromURL}, hash: 16F196FBA61BEE393113A7982C4BC6EC
     @Test
     void testGetCSVFileFromURL() throws IOException {
         byte[] result = rdfToCSVWService.getCSVFileFromURL("https://raw.githubusercontent.com/LadyMalande/RDFtoCSV/refs/heads/main/idOutuput.ttl", new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testGetCSVFileFromFile}, hash: D727BAC0885F82ECDD192455027D5852
     @Test
     void testGetCSVFileFromFile() throws IOException {
         when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
         when(mockMultipartFile.getOriginalFilename()).thenReturn("test.nt");
         byte[] result = rdfToCSVWService.getCSVFileFromFile(mockMultipartFile, new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testGetMetadataFileFromURL}, hash: 629543793994451CC24B0BEF8F642FB8
     @Test
     void testGetMetadataFileFromURL() throws IOException {
         byte[] result = rdfToCSVWService.getMetadataFileFromURL("http://example.com/test.nt", new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testGetMetadataFileFromFile}, hash: D19F926938BD2E238BFFBF4B6B604AD6
     @Test
     void testGetMetadataFileFromFile() throws IOException {
         when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
         when(mockMultipartFile.getOriginalFilename()).thenReturn("test.nt");
         byte[] result = rdfToCSVWService.getMetadataFileFromFile(mockMultipartFile, new HashMap<>());
         assertNotNull(result);
     }

     //BaseRock generated method id: ${testAdjustFilePathWithRandomNumber}, hash: 5E0B06BD83C4CCCCC05166562D9E5F6A
     @Test
     void testAdjustFilePathWithRandomNumber() throws Exception {
         Path originalPath = Paths.get("test.txt");
         Path result = (Path) invokePrivateMethod(rdfToCSVWService, "adjustFilePathWithRandomNumber", originalPath);
         assertNotNull(result);
         assertTrue(result.toString().matches("test_\\d+\\.txt"));
     }

     //BaseRock generated method id: ${testAdjustDirectoryPathWithRandomNumber}, hash: 01D1472352C35F570D71F43BE080552A
     @Test
     void testAdjustDirectoryPathWithRandomNumber() throws Exception {
         Path originalPath = Paths.get("/parent/child/test.txt");
         Path result = (Path) invokePrivateMethod(rdfToCSVWService, "adjustDirectoryPathWithRandomNumber", originalPath);
         assertNotNull(result);
         assertTrue(result.toString().matches("/parent/child_\\d+/test\\.txt"));
     }

     private Object invokePrivateMethod(Object object, String methodName, Object... args) throws Exception {
         Method method = object.getClass().getDeclaredMethod(methodName, args[0].getClass());
         method.setAccessible(true);
         return method.invoke(object, args);
     }
}