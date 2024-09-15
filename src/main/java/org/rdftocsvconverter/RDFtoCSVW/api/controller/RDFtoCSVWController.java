package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.awt.print.Book;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Tag(name = "RDF to CSV", description = "API for calling available parts of the output for conversion of RDF data to CSV on the Web")
@RestController
public class RDFtoCSVWController {

    private RDFtoCSVWService rdFtoCSVWService;

    @Autowired
    public RDFtoCSVWController(RDFtoCSVWService rdFtoCSVWService){
        this.rdFtoCSVWService = rdFtoCSVWService;
    }

    @GetMapping("/")
    public ResponseEntity<String> sanityCheck() {
        String responseMessage = "Hello from RDFtoCSV";

        // Return response with appropriate status
        return ResponseEntity.ok(responseMessage);
    }

    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    @PostMapping("/rdftocsvw")
    public byte[] getCSVW(@RequestParam("file") MultipartFile file, @RequestParam("fileURL") String fileURL, @RequestParam("choice") String choice){
        System.out.println("Got params for /rdftocsvw : " + file + " fileURL = " + fileURL + " choice=" + choice);

        try {
            if(file != null && fileURL != null){
                return rdFtoCSVWService.getCSVW(null, fileURL, choice);
            } else{
                return rdFtoCSVWService.getCSVW(file, fileURL, choice);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/csv/string")
    public ResponseEntity<String> convertRDFToCSV(
            @RequestParam("url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv/string with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getCSVString(url, config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IOException ex){
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    @PostMapping("/csv/string")
    public ResponseEntity<String> convertRDFToCSV(
            @RequestParam("file") MultipartFile file,  // Required file parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv/string with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            String responseMessage = rdFtoCSVWService.getCSVStringFromFile(file, config);
            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    @PostMapping("/csv")
    public ResponseEntity<byte[]> convertRDFToCSVFile(
            @RequestParam("file") MultipartFile file,  // Required file parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getCSVFileFromFile(file, config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> convertRDFToCSVFile(
            @RequestParam("url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getCSVFileFromURL(url, config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/metadata")
    public ResponseEntity<byte[]> convertRDFToCSVWMetadataFile(
            @RequestParam("file") MultipartFile file,  // Required file parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getMetadataFileFromFile(file, config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/metadata")
    public ResponseEntity<byte[]> convertRDFToCSVWMetadataFile(
            @RequestParam("url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getMetadataFileFromURL(url, config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/metadata/string")
    public ResponseEntity<String> convertRDFToCSVWMetadata(
            @RequestParam("url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv/string with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getMetadataString(url, config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IOException ex){
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    @Operation(summary = "Get metadata.json as string", description = "Get the contents of generated metadata.json as string, that was created by conversion of the given RDF file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated metadata.json contents",
                    content = { @Content(mediaType = "text/plain;charset=UTF-8")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @PostMapping("/metadata/string")
    public ResponseEntity<String> convertRDFToCSVWMetadata(
            @RequestParam("file") MultipartFile file,  // Required file parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "param2", required = false) String param2,
            @RequestParam(value = "param3", required = false) String param3,
            @RequestParam(value = "param4", required = false) String param4,
            @RequestParam(value = "param5", required = false) String param5,
            @RequestParam(value = "param6", required = false) String param6) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv/string with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, param3, param4, param5, param6);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            String responseMessage = rdFtoCSVWService.getCSVStringFromFile(file, config);
            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }


}
