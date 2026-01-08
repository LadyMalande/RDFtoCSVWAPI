package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;
import org.rdftocsvconverter.RDFtoCSVW.enums.ParsingChoice;
import org.rdftocsvconverter.RDFtoCSVW.enums.TableChoice;
import org.rdftocsvconverter.RDFtoCSVW.model.ComputationTask;
import org.rdftocsvconverter.RDFtoCSVW.model.SessionResponse;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.rdftocsvconverter.RDFtoCSVW.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for RDF to CSVW conversion endpoints.
 * Provides various endpoints to convert RDF data to CSV/CSVW format via file upload or URL.
 * Supports synchronous and asynchronous conversion operations.
 */
@Tag(name = "RDF to CSV", description = "API for calling available parts of the output for conversion of RDF data to CSV on the Web")
@RestController
public class RDFtoCSVWController {

    /**
     * Logger instance for this controller.
     */
    private static final Logger logger = LoggerFactory.getLogger(RDFtoCSVWController.class);

    /**
     * Service handling RDF to CSV/CSVW conversion operations.
     */
    private final RDFtoCSVWService rdFtoCSVWService;
    
    /**
     * Service for managing asynchronous computation tasks.
     */
    private final TaskService taskService;
    
    /**
     * Redis template for direct Redis operations and health checks.
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis connection URL from environment variable (production/Render.com).
     */
    @Value("${REDIS_URL:NOT_SET}")
    private String redisUrl;

    /**
     * Redis host from environment variable (local development fallback).
     */
    @Value("${SPRING_REDIS_HOST:NOT_SET}")
    private String redisHost;

    /**
     * Redis port from environment variable (local development fallback).
     */
    @Value("${SPRING_REDIS_PORT:0}")
    private int redisPort;

    /**
     * Instantiates a new RDFtoCSVWController with endpoints and methods to get converter results.
     *
     * @param rDFtoCSVWService the RDFtoCSVWService with methods to help the mapped endpoints to serve the conversion.
     * @param taskService      the TaskService for managing computation tasks
     * @param redisTemplate    the RedisTemplate for direct Redis operations
     */
    @Autowired
    public RDFtoCSVWController(RDFtoCSVWService rDFtoCSVWService, TaskService taskService, RedisTemplate<String, Object> redisTemplate){
        this.rdFtoCSVWService = rDFtoCSVWService;
        this.taskService = taskService;
        this.redisTemplate = redisTemplate;
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
        logger.info("Health check requested for Redis");
        try {
            logger.debug("Attempting to ping Redis...");
            // Set a shorter timeout for health checks using connection test
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            logger.info("Redis health check successful. Response: {}", pong);
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "message", "Redis is connected and accessible"
            ));
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            logger.error("Redis connection failure during health check", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "DOWN",
                    "message", "Cannot connect to Redis",
                    "error", e.getMessage(),
                    "errorType", e.getClass().getSimpleName()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during Redis health check", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "ERROR",
                    "message", "Error checking Redis connection",
                    "error", e.getMessage(),
                    "errorType", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Debug endpoint to check Redis configuration.
     *
     * @return the response entity with Redis configuration details
     */
    @io.swagger.v3.oas.annotations.Hidden
    @Operation(summary = "Debug Redis configuration", description = "Shows Redis configuration details for debugging.")
    @GetMapping("/debug/redis-config")
    @Profile({"dev", "test"})  // Only active in dev and test environments, settable via 'spring.profiles.active' property in application.properties
    public ResponseEntity<Map<String, String>> debugRedisConfig() {
        try {
            String connectionInfo = redisTemplate.getConnectionFactory().getConnection().toString();
            
            return ResponseEntity.ok(Map.of(
                    "redisUrlConfigured", redisUrl != null && !redisUrl.equals("NOT_SET") ? "YES (length: " + redisUrl.length() + ")" : "NO",
                    "redisHost", redisHost,
                    "redisPort", String.valueOf(redisPort),
                    "connectionFactory", connectionInfo,
                    "note", "Check application logs for detailed Redis configuration"
            ));
        } catch (Exception e) {
            logger.error("Error getting Redis debug info", e);
            return ResponseEntity.ok(Map.of(
                    "redisUrlConfigured", redisUrl != null && !redisUrl.equals("NOT_SET") ? "YES" : "NO",
                    "redisHost", redisHost,
                    "redisPort", String.valueOf(redisPort),
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
        
        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        
        try {
            AppConfig config;
            if (file != null) {
                // Validate file type for streaming methods
                validateFileTypeForStreamingMethods(file.getOriginalFilename(), parsingMethod);
                // Build config from uploaded file
                config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);
                return rdFtoCSVWService.getZipFile(config);
            } else if (fileURL != null && !fileURL.isEmpty()) {
                // Validate URL to prevent SSRF
                validateUrl(fileURL);
                // Validate file type for streaming methods
                validateFileTypeForStreamingMethods(fileURL, parsingMethod);
                // Build config from URL
                config = rdFtoCSVWService.buildAppConfig(fileURL, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);
                return rdFtoCSVWService.getZipFile(config);
            } else {
                throw new IllegalArgumentException("Either file or fileURL must be provided");
            }
        } catch (IllegalArgumentException e) {
            throw e;
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

        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        validateUrl(url);
        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(url, conversionMethod);

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(conversionMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getCSVString(config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IllegalArgumentException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
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

        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(file.getOriginalFilename(), parsingMethod);

        AppConfig config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);
        //Map<String, String> config = rdFtoCSVWService.prepareConfigParameter(String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention).get();

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            String responseMessage = rdFtoCSVWService.getCSVStringFromFile(config);
            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
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

        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(file.getOriginalFilename(), parsingMethod);

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
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
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

        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        validateUrl(url);
        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(url, parsingMethod);

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getCSVFileFromURL(config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
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

        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(file.getOriginalFilename(), parsingMethod);

        AppConfig config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getMetadataFileFromFile(config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
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

        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        validateUrl(url);
        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(url, parsingMethod);

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            byte[] generatedFile = rdFtoCSVWService.getMetadataFileFromURL(config);
            // Return response with appropriate status
            return ResponseEntity.ok(generatedFile);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
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

        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        validateUrl(url);
        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(url, parsingMethod);

        AppConfig config = rdFtoCSVWService.buildAppConfig(url, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            String responseMessage = rdFtoCSVWService.getMetadataString(config);

            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch(IllegalArgumentException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
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

        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        // Validate file type for streaming methods
        validateFileTypeForStreamingMethods(file.getOriginalFilename(), parsingMethod);

        AppConfig config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), firstNormalForm, preferredLanguages, namingConvention);

        // Example of using the parameters
        try {
            // Assuming getCSVString method can handle file and URL as needed
            String responseMessage = rdFtoCSVWService.getMetadataStringFromFile(config);
            // Return response with appropriate status
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
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
        
        // Validate input parameters
        validatePreferredLanguages(preferredLanguages);
        validateNamingConvention(namingConvention);
        
        try {
            // Create a new task and get session ID
            String sessionId = taskService.createTask();
            
            // Build config
            AppConfig config;
            if (file != null) {
                // Validate file type for streaming methods
                validateFileTypeForStreamingMethods(file.getOriginalFilename(), parsingMethod);
                config = rdFtoCSVWService.buildAppConfig(file, String.valueOf(table), String.valueOf(parsingMethod), 
                        firstNormalForm, preferredLanguages, namingConvention);
            } else if (fileURL != null && !fileURL.isEmpty()) {
                // Validate URL to prevent SSRF
                validateUrl(fileURL);
                // Validate file type for streaming methods
                validateFileTypeForStreamingMethods(fileURL, parsingMethod);
                config = rdFtoCSVWService.buildAppConfig(fileURL, String.valueOf(table), String.valueOf(parsingMethod), 
                        firstNormalForm, preferredLanguages, namingConvention);
            } else {
                return ResponseEntity.badRequest().body(new SessionResponse(null, "Either file or fileURL must be provided"));
            }
            
            // Start async computation
            rdFtoCSVWService.computeAsyncAndStore(sessionId, config);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SessionResponse(sessionId));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SessionResponse(null, e.getMessage()));
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
    @GetMapping(value = "/status/{sessionId}", produces = {"application/octet-stream", "application/json"})
    public ResponseEntity<?> getComputationStatus(@PathVariable String sessionId) {
        ComputationTask task = taskService.getTask(sessionId);
        
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "status", "COMPUTING",
                                "sessionId", sessionId,
                                "message", "Computation is still in progress"
                        ));
                
            case FAILED:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "status", "FAILED",
                                "sessionId", sessionId,
                                "error", task.getErrorMessage() != null ? task.getErrorMessage() : "Unknown error"
                        ));
                
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(Map.of("error", "Unknown status", "sessionId", sessionId));
        }
    }

    /**
     * Validates that STREAMING and BIGFILESTREAMING methods are only used with .nt files.
     *
     * @param filename the name of the file or URL
     * @param parsingMethod the parsing/conversion method
     * @throws IllegalArgumentException if STREAMING/BIGFILESTREAMING is used with non-.nt file
     */
    private void validateFileTypeForStreamingMethods(String filename, ParsingChoice parsingMethod) {
        if ((parsingMethod == ParsingChoice.STREAMING || parsingMethod == ParsingChoice.BIGFILESTREAMING)) {
            if (filename == null || !filename.toLowerCase().endsWith(".nt")) {
                throw new IllegalArgumentException(
                    "STREAMING and BIGFILESTREAMING conversion methods only work with .nt files. " +
                    "Please use RDF4J method for other file types or provide a .nt file."
                );
            }
        }
    }

    /**
     * Validates URL to prevent SSRF attacks.
     * Only allows http/https protocols and blocks private IP ranges.
     *
     * @param url the URL to validate
     * @throws IllegalArgumentException if URL is invalid or potentially malicious
     */
    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        try {
            java.net.URI uri = new java.net.URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            // Only allow http and https protocols
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Only HTTP and HTTPS protocols are allowed");
            }

            // Block localhost and private IP ranges
            if (host == null) {
                throw new IllegalArgumentException("Invalid URL: missing host");
            }

            String hostLower = host.toLowerCase();
            if (hostLower.equals("localhost") || 
                hostLower.equals("127.0.0.1") ||
                hostLower.equals("0.0.0.0") ||
                hostLower.startsWith("192.168.") ||
                hostLower.startsWith("10.") ||
                hostLower.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*") ||
                hostLower.equals("::1") ||
                hostLower.startsWith("169.254.")) {
                throw new IllegalArgumentException("Access to private IP ranges and localhost is not allowed");
            }

        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
        }
    }

    /**
     * Sanitizes filename to prevent path traversal attacks.
     *
     * @param filename the filename to sanitize
     * @return sanitized filename
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        // Remove path separators and parent directory references
        return filename.replaceAll("[/\\\\]", "_")
                      .replaceAll("\\.\\.", "_")
                      .trim();
    }

    /**
     * Validates preferred languages parameter.
     * Accepts comma-separated language codes (2-3 lowercase letters).
     *
     * @param preferredLanguages comma-separated language codes
     * @throws IllegalArgumentException if format is invalid
     */
    private void validatePreferredLanguages(String preferredLanguages) {
        if (preferredLanguages == null || preferredLanguages.trim().isEmpty()) {
            return; // Empty is allowed
        }

        // Pattern: 2-3 lowercase letters, comma-separated, optional spaces
        // Examples: "en", "en,cs", "en, cs, de"
        if (!preferredLanguages.matches("^[a-z]{2,3}(\\s*,\\s*[a-z]{2,3})*$")) {
            throw new IllegalArgumentException(
                "Invalid preferred languages format. Expected comma-separated 2-3 letter language codes (e.g., 'en,cs,de'). " +
                "Received: " + preferredLanguages
            );
        }
    }

    /**
     * Validates naming convention parameter.
     * Only allows predefined naming conventions from AppConfig constants.
     *
     * @param namingConvention the naming convention to validate
     * @throws IllegalArgumentException if naming convention is not recognized
     */
    private void validateNamingConvention(String namingConvention) {
        if (namingConvention == null || namingConvention.trim().isEmpty()) {
            return; // Empty is allowed (will use default)
        }

        // List of allowed naming conventions
        java.util.Set<String> allowedConventions = java.util.Set.of(
            AppConfig.COLUMN_NAMING_CAMEL_CASE,
            AppConfig.COLUMN_NAMING_PASCAL_CASE,
            AppConfig.COLUMN_NAMING_SNAKE_CASE,
            AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,
            AppConfig.COLUMN_NAMING_KEBAB_CASE,
            AppConfig.COLUMN_NAMING_TITLE_CASE,
            AppConfig.COLUMN_NAMING_DOT_NOTATION,
            AppConfig.ORIGINAL_NAMING_NOTATION
        );

        if (!allowedConventions.contains(namingConvention)) {
            throw new IllegalArgumentException(
                "Invalid naming convention. Allowed values: " + String.join(", ", allowedConventions) +
                ". Received: " + namingConvention
            );
        }
    }

}
