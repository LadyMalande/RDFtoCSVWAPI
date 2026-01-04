package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;
import org.rdftocsvconverter.RDFtoCSVW.enums.ParsingChoice;
import org.rdftocsvconverter.RDFtoCSVW.enums.TableChoice;
import org.rdftocsvconverter.RDFtoCSVW.model.ComputationTask;
import org.rdftocsvconverter.RDFtoCSVW.model.SessionResponse;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.rdftocsvconverter.RDFtoCSVW.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * The RDFtoCSVWController class that contains GET/POST methods to initiate conversions and get send results back.
 */
@Tag(name = "RDF to CSV", description = "API for calling available parts of the output for conversion of RDF data to CSV on the Web")
@RestController
public class RDFtoCSVWController {

    private final RDFtoCSVWService rdFtoCSVWService;
    private final TaskService taskService;

    /**
     * Instantiates a new RDFtoCSVWController with endpoints and methods to get converter results.
     *
     * @param rDFtoCSVWService the RDFtoCSVWService with methods to help the mapped endpoints to serve the conversion.
     * @param taskService      the TaskService for managing computation tasks
     */
    @Autowired
    public RDFtoCSVWController(RDFtoCSVWService rDFtoCSVWService, TaskService taskService){
        this.rdFtoCSVWService = rDFtoCSVWService;
        this.taskService = taskService;
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
     * Check Redis connection health.
     *
     * @return the response entity with Redis status
     */
    @Operation(summary = "Check Redis connection", description = "Verify if Redis is accessible and working properly.")
    @GetMapping("/health/redis")
    public ResponseEntity<Map<String, String>> checkRedisHealth() {
        try {
            String testKey = "health-check-" + System.currentTimeMillis();
            taskService.getTask(testKey); // This will attempt to connect to Redis
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "message", "Redis is connected and accessible"
            ));
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "DOWN",
                    "message", "Cannot connect to Redis",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "ERROR",
                    "message", "Error checking Redis connection",
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get CSVW byte [] (.zip) format containing all converted files - CSVs and .json metadata file.
     *
     * @param file               The RDF file to convert
     * @param fileURL            The RDF file url to convert
     * @param parsingMethod      The choice of parsing method
     * @param table              How many tables to produce - one or more
     * @param firstNormalForm    The first normal form - whether to produce the resulting CSVs in 1NF (cells containing atomic values)
     * @param preferredLanguages Comma-separated list of preferred language codes (e.g., 'en,cs,de')
     * @param namingConvention   Naming convention for CSV headers
     * @return byte [] Returns .zip format of all the converted parts - CSVs and .json metadata file.
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
    public byte[] getCSVW(@RequestParam(value = "file", required = false) MultipartFile file,
                          @RequestParam(value = "fileURL", required = false) String fileURL,
                          @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
                          @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table,
                          @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
                          @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
                          @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
                          @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                                  "camelCase",
                                  "PascalCase",
                                  "snake_case",
                                  "SCREAMING_SNAKE_CASE",
                                  "kebab-case",
                                  "Title Case",
                                  "dot.notation",
                                  "original"
                          }, example = "camelCase"))
                          @RequestParam(value = "namingConvention", required = false) String namingConvention) throws IOException {
        System.out.println("Got params for /rdftocsvw : file=" + file + " fileURL=" + fileURL + " conversionMethod=" + parsingMethod);
        
        try {
            AppConfig config;
            if (file != null) {
                // Build config from uploaded file
                config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);
                return rdFtoCSVWService.getZipFile(config);
            } else if (fileURL != null && !fileURL.isEmpty()) {
                // Build config from URL
                config = rdFtoCSVWService.buildAppConfig(fileURL, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);
                return rdFtoCSVWService.getZipFile(config);
            } else {
                throw new IllegalArgumentException("Either file or fileURL must be provided");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing RDF to CSVW conversion", e);
        }
    }

    /**
     * Convert rdf to csv response entity.
     *
     * @param url              the url of RDF file for conversion
     * @param table            the parameter for choosing how many tables to create
     * @param conversionMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the Response entity
     */
    @Operation(summary = "Get converted CSV file as string response", description = "Get the contents of generated rdf-data.csv as string, that was created by conversion of the given RDF data URL. If the option to generate more tables is chosen and the output produces more tables, all the CSV files string outputs will be in one file, visually separated in vertical succession.")
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice conversionMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    "camelCase",
                    "PascalCase",
                    "snake_case",
                    "SCREAMING_SNAKE_CASE",
                    "kebab-case",
                    "Title Case",
                    "dot.notation",
                    "original"
            }, example = "camelCase"))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv/string with URL: " + url);

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(conversionMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getCSVString(config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IOException ex){
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    /**
     * Convert rdf to csv string.
     *
     * @param file            the RDF file to convert
     * @param table            the parameter for choosing how many tables to create
     * @param parsingMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the response entity returning CSV string
     * @throws IOException 
     */
    @Operation(summary = "Get converted CSV file as string", description = "Get the contents of generated rdf-data.csv as string, that was created by conversion of the given RDF file. If the option to generate more tables is chosen and the output produces more tables, all the CSV files string outputs will be in one file, visually separated in vertical succession.")
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    "camelCase",
                    "PascalCase",
                    "snake_case",
                    "SCREAMING_SNAKE_CASE",
                    "kebab-case",
                    "Title Case",
                    "dot.notation",
                    "original"
            }, example = "camelCase"))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) throws ExecutionException, InterruptedException, IOException {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv/string with file: " + file.getName());

        AppConfig config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);
        //Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention).get();

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            String responseMessage = rdFtoCSVWService.getCSVStringFromFile(config);
            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    /**
     * Convert rdf to csv file .
     *
     * @param file            the RDF file to convert
     * @param table            the parameter for choosing how many tables to create
     * @param parsingMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the response entity returning CSV file
     */
    @Operation(summary = "Get converted CSV file as file", description = "Get the contents of generated rdf-data.csv as file, that was created by conversion of the given RDF file")
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    "camelCase",
                    "PascalCase",
                    "snake_case",
                    "SCREAMING_SNAKE_CASE",
                    "kebab-case",
                    "Title Case",
                    "dot.notation",
                    "original"
            }, example = "camelCase"))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) throws IOException {

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv with file: " + file.getName());

        AppConfig config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getCSVFileFromFile(config);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=yourfilename.ext");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Convert rdf to csv file response entity.
     *
     * @param url              the url of RDF file for conversion
     * @param table            the parameter for choosing how many tables to create
     * @param parsingMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the Response entity returning CSV file
     */
    @Operation(summary = "Get converted CSV file as file", description = "Get the contents of generated rdf-data.csv as file, that was created by conversion of the given RDF data URL")
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    AppConfig.COLUMN_NAMING_CAMEL_CASE,
                    AppConfig.COLUMN_NAMING_PASCAL_CASE,
                    AppConfig.COLUMN_NAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_KEBAB_CASE,
                    AppConfig.COLUMN_NAMING_TITLE_CASE,
                    AppConfig.COLUMN_NAMING_DOT_NOTATION,
                    AppConfig.ORIGINAL_NAMING_NOTATION
            }, example = AppConfig.COLUMN_NAMING_CAMEL_CASE))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) throws ExecutionException, InterruptedException {

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getCSVFileFromURL(config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Convert rdf to csvw metadata file response entity.
     *
     * @param file            the RDF file to convert
     * @param table            the parameter for choosing how many tables to create
     * @param parsingMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the response entity returning metadata file
     */
    @Operation(summary = "Get created metadata as file", description = "Get the contents of generated metadata.json as file, that was created by conversion of the given RDF file in the body of the request")
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    AppConfig.COLUMN_NAMING_CAMEL_CASE,
                    AppConfig.COLUMN_NAMING_PASCAL_CASE,
                    AppConfig.COLUMN_NAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_KEBAB_CASE,
                    AppConfig.COLUMN_NAMING_TITLE_CASE,
                    AppConfig.COLUMN_NAMING_DOT_NOTATION,
                    AppConfig.ORIGINAL_NAMING_NOTATION
            }, example = AppConfig.COLUMN_NAMING_CAMEL_CASE))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) throws IOException {

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv with file: " + file.getName());

        AppConfig config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getMetadataFileFromFile(config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Convert rdf to csvw metadata file response entity.
     *
     * @param url              the url of RDF file for conversion
     * @param table            the parameter for choosing how many tables to create
     * @param parsingMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the Response entity returning metadata file
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    AppConfig.COLUMN_NAMING_CAMEL_CASE,
                    AppConfig.COLUMN_NAMING_PASCAL_CASE,
                    AppConfig.COLUMN_NAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_KEBAB_CASE,
                    AppConfig.COLUMN_NAMING_TITLE_CASE,
                    AppConfig.COLUMN_NAMING_DOT_NOTATION,
                    AppConfig.ORIGINAL_NAMING_NOTATION
            }, example = AppConfig.COLUMN_NAMING_CAMEL_CASE))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) throws ExecutionException, InterruptedException {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv with URL: " + url);

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getMetadataFileFromURL(config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Convert rdf to csvw metadata response entity.
     *
     * @param url              the url of RDF file for conversion
     * @param table            the parameter for choosing how many tables to create
     * @param parsingMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the Response entity returning metadata as string in response
     */
    @Operation(summary = "Get  created metadata as string response", description = "Get the contents of generated metadata.json as string, that was created by conversion of the given RDF data URL")
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    AppConfig.COLUMN_NAMING_CAMEL_CASE,
                    AppConfig.COLUMN_NAMING_PASCAL_CASE,
                    AppConfig.COLUMN_NAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_KEBAB_CASE,
                    AppConfig.COLUMN_NAMING_TITLE_CASE,
                    AppConfig.COLUMN_NAMING_DOT_NOTATION,
                    AppConfig.ORIGINAL_NAMING_NOTATION
            }, example = AppConfig.COLUMN_NAMING_CAMEL_CASE))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) throws ExecutionException, InterruptedException {

        // Log the incoming request
        System.out.println("Received GET request for /rdftocsv/string with URL: " + url);

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getMetadataString(config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IOException ex){
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    /**
     * Convert rdf to csvw metadata string.
     *
     * @param file            the RDF file to convert
     * @param table            the parameter for choosing how many tables to create
     * @param parsingMethod the conversion method choosing from RDF4J/STREAMING/BIGFILESTREAMING
     * @param firstNormalForm  if true, the final tables will be in first normal form
     * @return the response entity returning metadata string
     */
    @Operation(summary = "Get  created metadata as string response", description = "Get the contents of generated metadata.json as string, that was created by conversion of the given RDF file")
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
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table, // Optional parameters
            //@Parameter(name = "table", description = "Number of created CSV tables", schema=@Schema(type="string", allowableValues={"one","more"}, defaultValue = "one")) TableChoice table,
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')", schema = @Schema(type = "string", example = "en,cs"))
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    AppConfig.COLUMN_NAMING_CAMEL_CASE,
                    AppConfig.COLUMN_NAMING_PASCAL_CASE,
                    AppConfig.COLUMN_NAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_KEBAB_CASE,
                    AppConfig.COLUMN_NAMING_TITLE_CASE,
                    AppConfig.COLUMN_NAMING_DOT_NOTATION,
                    AppConfig.ORIGINAL_NAMING_NOTATION
            }, example = AppConfig.COLUMN_NAMING_CAMEL_CASE))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) throws ExecutionException, InterruptedException, IOException {  // Optional file parameter

        // Log the incoming request
        System.out.println("Received POST request for /rdftocsv/string with file: " + file.getName());

        AppConfig config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            String responseMessage = rdFtoCSVWService.getMetadataStringFromFile(config);
            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("There has been a problem with parsing your request");
        }
    }

    /**
     * Asynchronously convert RDF to CSVW and return a session ID for tracking.
     *
     * @param file               The RDF file to convert
     * @param fileURL            The RDF file url to convert
     * @param parsingMethod      The choice of parsing method
     * @param table              How many tables to produce - one or more
     * @param firstNormalForm    The first normal form - whether to produce the resulting CSVs in 1NF
     * @param preferredLanguages Comma-separated list of preferred language codes
     * @param namingConvention   Naming convention for CSV headers
     * @return SessionResponse with session ID
     */
    @Operation(summary = "Start async CSVW conversion", description = "Initiates an asynchronous RDF to CSVW conversion and returns a session ID to track the computation status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Computation started, session ID returned",
                    content = { @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content)
    })
    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    @PostMapping("/rdftocsvw/async")
    public ResponseEntity<SessionResponse> convertRDFToCSVWAsync(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "fileURL", required = false) String fileURL,
            @RequestParam(value = "conversionMethod", required = false, defaultValue = "RDF4J") ParsingChoice parsingMethod,
            @RequestParam(value = "table", required = false, defaultValue = "ONE") TableChoice table,
            @RequestParam(value = "firstNormalForm", required = false, defaultValue = "false") Boolean firstNormalForm,
            @Parameter(description = "Comma-separated list of preferred language codes (e.g., 'en,cs,de')")
            @RequestParam(value = "preferredLanguages", required = false) String preferredLanguages,
            @Parameter(description = "Naming convention for CSV headers", schema = @Schema(type = "string", allowableValues = {
                    AppConfig.COLUMN_NAMING_CAMEL_CASE,
                    AppConfig.COLUMN_NAMING_PASCAL_CASE,
                    AppConfig.COLUMN_NAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,
                    AppConfig.COLUMN_NAMING_KEBAB_CASE,
                    AppConfig.COLUMN_NAMING_TITLE_CASE,
                    AppConfig.COLUMN_NAMING_DOT_NOTATION,
                    AppConfig.ORIGINAL_NAMING_NOTATION
            }, example = AppConfig.COLUMN_NAMING_CAMEL_CASE))
            @RequestParam(value = "namingConvention", required = false) String namingConvention) 
            throws IOException {
        
        System.out.println("Received async request for /rdftocsvw/async");
        
        try {
            // Create a new task and get session ID
            String sessionId = taskService.createTask();
            
            // Build config
            AppConfig config;
            if (file != null) {
                config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), 
                        firstNormalForm, preferredLanguages, namingConvention);
            } else if (fileURL != null && !fileURL.isEmpty()) {
                config = rdFtoCSVWService.buildAppConfig(fileURL, String.valueOf(table), String.valueOf(parsingMethod), 
                        firstNormalForm, preferredLanguages, namingConvention);
            } else {
                return ResponseEntity.badRequest().body(new SessionResponse(null, "Either file or fileURL must be provided"));
            }
            
            // Start async computation
            rdFtoCSVWService.computeAsyncAndStore(sessionId, config);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SessionResponse(sessionId));
            
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            System.err.println("Redis connection failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new SessionResponse(null, "Redis is not available. Please ensure Redis is running. If using Docker, run: docker-compose up -d"));
        } catch (Exception e) {
            System.err.println("Error starting async computation: " + e.getMessage());
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Redis")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new SessionResponse(null, "Unable to connect to Redis. Please ensure Redis is running."));
            }
            return ResponseEntity.badRequest().body(new SessionResponse(null, "Error: " + errorMsg));
        }
    }

    /**
     * Get the status and result of a computation by session ID.
     *
     * @param sessionId the session ID
     * @return ResponseEntity with status and result if available
     */
    @Operation(summary = "Get computation status", description = "Retrieve the status of an ongoing or completed computation. Returns COMPUTING, DONE (with result), or FAILED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Computation completed, returns ZIP file",
                    content = { @Content(mediaType = "application/octet-stream")}),
            @ApiResponse(responseCode = "202", description = "Computation still in progress",
                    content = { @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Computation failed",
                    content = { @Content(mediaType = "application/json")})
    })
    @GetMapping("/status/{sessionId}")
    public ResponseEntity<?> getComputationStatus(@PathVariable String sessionId) {
        ComputationTask task = taskService.getTask(sessionId);
        
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Session not found", "sessionId", sessionId));
        }
        
        switch (task.getStatus()) {
            case DONE:
                // Return the ZIP file
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=csvw-output.zip");
                headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(task.getResult());
                
            case COMPUTING:
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of(
                                "status", "COMPUTING",
                                "sessionId", sessionId,
                                "message", "Computation is still in progress"
                        ));
                
            case FAILED:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "status", "FAILED",
                                "sessionId", sessionId,
                                "error", task.getErrorMessage() != null ? task.getErrorMessage() : "Unknown error"
                        ));
                
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Unknown status", "sessionId", sessionId));
        }
    }

}
