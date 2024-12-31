package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * The RDFtoCSVWController class that contains GET/POST methods to initiate conversions and get send results back.
 */
@Tag(name = "RDF to CSV", description = "API for calling available parts of the output for conversion of RDF data to CSV on the Web")
@RestController
public class RDFtoCSVWController {

    private final RDFtoCSVWService rdFtoCSVWService;

    /**
     * Instantiates a new RDFtoCSVWController with endpoints and methods to get converter results.
     *
     * @param rDFtoCSVWService the RDFtoCSVWService with methods to help the mapped endpoints to serve the conversion.
     */
    @Autowired
    public RDFtoCSVWController(RDFtoCSVWService rDFtoCSVWService){
        this.rdFtoCSVWService = rDFtoCSVWService;
    }

    /**
     * Sanity check response entity. For checking whether the service is up and running and available for other operations.
     *
     * @return the response entity returning a welcoming String
     */
    @Operation(summary = "Get welcoming string", description = "Get a welcoming string to test the availability of the web service easily.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Welcoming string is returned as text.",
                    content = { @Content(mediaType = "text/plain;charset=UTF-8")}),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part, service is momentarily unavailable.",
                    content = @Content) })
    @GetMapping("/")
    public ResponseEntity<String> sanityCheck() {
        String responseMessage = "Hello from RDFtoCSV";

        // Return response with appropriate status
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Get CSVW byte [ ] (.zip) format containing all converted files - CSVs and .json metadata file.
     *
     * @param file            The RDF file to convert
     * @param fileURL         The RDF file url to convert
     * @param choice          The choice of parsing method
     * @param tables          How many tables to produce - one or more
     * @param firstNormalForm The first normal form - whether to produce the resulting CSVs in 1NF (cells containing atomic values)
     * @return byte [ ] Returns .zip format of all the converted parts - CSVs and .json metadata file.
     */
    @Operation(summary = "Get full CSVW content as .zip", description = "Get the generated rdf-data.csv(s) as file(s) along with appropriate metadata.json file, all of them zipped in a ZIP archive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns ZIP file containing x CSV files and metadata.json file",
                    content = { @Content(mediaType = "application/octet-stream")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    @PostMapping("/rdftocsvw")
    public byte[] getCSVW(@RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "fileURL", required = false) String fileURL,
                          //@Parameter(description = "The number of CSV tables created during conversion", schema = @Schema(implementation = TableChoice.class))
                          //@Parameter(description = "The number of CSV tables created during conversion", example = "ONE")
                          @RequestParam(value = "choice", required = false) String choice,
                          @RequestParam(value = "tables", required = false) String tables,
                          @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm){
        System.out.println("Got params for /rdftocsvw : " + file + " fileURL = " + fileURL + " choice=" + choice);
        try {
            if(file != null && !fileURL.isEmpty()){
                System.out.println("Got params for /rdftocsvw : file=" + file + " fileURL = " + fileURL + " choice=" + choice + " file != null && !fileURL.isEmpty()");

                return rdFtoCSVWService.getCSVW(null, fileURL, choice, tables, firstNormalForm);

            } else if(file != null){
                System.out.println("Got params for /rdftocsvw : file=" + file + " fileURL = " + fileURL + " choice=" + choice + " file != null branch");
                return rdFtoCSVWService.getCSVW(file, fileURL, choice, tables, firstNormalForm);
            } else{
                System.out.println("Got params for /rdftocsvw : file=null fileURL = " + fileURL + " choice=" + choice + " else branch");
                return rdFtoCSVWService.getCSVW(null, fileURL, choice, tables, firstNormalForm);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert rdf to csv response entity.
     *
     * @param url              the url
     * @param table            the table
     * @param conversionMethod the conversion method
     * @param firstNormalForm  the first normal form
     * @return the response entity
     */
    @Operation(summary = "Get rdf-data.csv as file", description = "Get the contents of generated rdf-data.csv as string, that was created by conversion of the given RDF data URL. If the option to generate more tables is chosen and the output produces more tables, all the CSV files string outputs will be in one file, visually separated in vertical succession.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated rdf-data.csv file",
                    content = { @Content(mediaType = "text/plain;charset=UTF-8")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @GetMapping("/csv/string")
    public ResponseEntity<String> convertRDFToCSV(
            @RequestParam("url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false) String conversionMethod,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv/string with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, conversionMethod, firstNormalForm);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getCSVString(url, config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IOException ex){
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    /**
     * Convert rdf to csv response entity.
     *
     * @param file            the file
     * @param table           the table
     * @param param2          the param 2
     * @param firstNormalForm the first normal form
     * @return the response entity
     */
    @Operation(summary = "Get rdf-data.csv as string", description = "Get the contents of generated rdf-data.csv as string, that was created by conversion of the given RDF file. If the option to generate more tables is chosen and the output produces more tables, all the CSV files string outputs will be in one file, visually separated in vertical succession.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated rdf-data.csv as string",
                    content = { @Content(mediaType = "text/plain;charset=UTF-8")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @PostMapping("/csv/string")
    public ResponseEntity<String> convertRDFToCSV(
            @RequestParam("file") MultipartFile file,  // Required file parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false) String param2,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv/string with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, firstNormalForm);

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

    /**
     * Convert rdf to csv file response entity.
     *
     * @param file            the file
     * @param table           the table
     * @param param2          the param 2
     * @param firstNormalForm the first normal form
     * @return the response entity
     */
    @Operation(summary = "Get rdf-data.csv as file", description = "Get the contents of generated rdf-data.csv as file, that was created by conversion of the given RDF file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated rdf-data.csv file",
                    content = { @Content(mediaType = "application/octet-stream")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @PostMapping("/csv")
    public ResponseEntity<byte[]> convertRDFToCSVFile(
            @RequestParam("file") MultipartFile file,  // Required file parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false) String param2,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, firstNormalForm);

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

    /**
     * Convert rdf to csv file response entity.
     *
     * @param url             the url
     * @param table           the table
     * @param param2          the param 2
     * @param firstNormalForm the first normal form
     * @return the response entity
     */
    @Operation(summary = "Get rdf-data.csv as file", description = "Get the contents of generated rdf-data.csv as file, that was created by conversion of the given RDF data URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated rdf-data.csv file",
                    content = { @Content(mediaType = "application/octet-stream")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @GetMapping("/csv")
    public ResponseEntity<byte[]> convertRDFToCSVFile(
            @RequestParam("url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false) String param2,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, firstNormalForm);

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

    /**
     * Convert rdf to csvw metadata file response entity.
     *
     * @param file            the file
     * @param table           the table
     * @param param2          the param 2
     * @param firstNormalForm the first normal form
     * @return the response entity
     */
    @Operation(summary = "Get metadata.json as file", description = "Get the contents of generated metadata.json as file, that was created by conversion of the given RDF file in the body of the request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated metadata.json file",
                    content = { @Content(mediaType = "application/octet-stream")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @PostMapping("/metadata")
    public ResponseEntity<byte[]> convertRDFToCSVWMetadataFile(
            @RequestParam(value = "file") MultipartFile file,  // Required file parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false) String param2,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, firstNormalForm);

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

    /**
     * Convert rdf to csvw metadata file response entity.
     *
     * @param url             the url
     * @param table           the table
     * @param param2          the param 2
     * @param firstNormalForm the first normal form
     * @return the response entity
     */
    @Operation(summary = "Get metadata.json as file", description = "Get the contents of generated metadata.json as file, that was created by conversion of the given RDF data URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated metadata.json file",
                    content = { @Content(mediaType = "application/octet-stream")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @GetMapping("/metadata")
    public ResponseEntity<byte[]> convertRDFToCSVWMetadataFile(
            @RequestParam(value = "url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false) String param2,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, firstNormalForm);

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

    /**
     * Convert rdf to csvw metadata response entity.
     *
     * @param url             the url
     * @param table           the table
     * @param param2          the param 2
     * @param firstNormalForm the first normal form
     * @return the response entity
     */
    @Operation(summary = "Get metadata.json as string", description = "Get the contents of generated metadata.json as string, that was created by conversion of the given RDF data URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated metadata.json contents",
                    content = { @Content(mediaType = "text/plain;charset=UTF-8")}),
            @ApiResponse(responseCode = "400", description = "Invalid RDF file",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Trouble at the backend part",
                    content = @Content) })
    @GetMapping("/metadata/string")
    public ResponseEntity<String> convertRDFToCSVWMetadata(
            @RequestParam(value = "url") String url,  // Required URL parameter
            @RequestParam(value = "table", required = false) String table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false) String param2,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv/string with URL: " + url);

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, firstNormalForm);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getMetadataString(url, config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IOException ex){
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    /**
     * Convert rdf to csvw metadata response entity.
     *
     * @param file            the file
     * @param table           the table
     * @param param2          the param 2
     * @param firstNormalForm the first normal form
     * @return the response entity
     */
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
            @RequestParam(value = "conversionMethod", required = false) String param2,
            @RequestParam(value = "firstNormalForm", required = false) Boolean firstNormalForm) {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv/string with file: " + file.getName());

        Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(table, param2, firstNormalForm);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            String responseMessage = rdFtoCSVWService.getMetadataStringFromFile(file, config);
            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }


}
