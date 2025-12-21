package org.rdftocsvconverter.RDFtoCSVW.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import org.rdftocsvconverter.RDFtoCSVW.enums.ParsingChoice;
import org.rdftocsvconverter.RDFtoCSVW.enums.TableChoice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;


/**
 * The class methods that handles API to RDFtoCSV library communication and parameter conversion.
 */
@Service
public class RDFtoCSVWService {
    /**
     * Get csvw byte [ ].
     *
     * @param multipartFile   the multipart file
     * @param fileURL         the file url
     * @param choice          the choice
     * @param table           the table
     * @param firstNormalForm the first normal form
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    @Async
    public CompletableFuture<byte[]> getCSVW(MultipartFile multipartFile, String fileURL, String choice, String table, Boolean firstNormalForm) throws IOException {
        File input = null;
        if (multipartFile != null) {
            System.out.println("multipartFile != null ");
            input = saveFile(multipartFile);
        }

        try {
            Class<?> clazz = Class.forName("org.eclipse.rdf4j.rio.nquads.NQuadsParserFactory");
            System.out.println("Class loaded: " + clazz.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }

        Map<String, String> configMap = null;
        if (table != null) {
            configMap = new HashMap<>();
            configMap.put("table", table);
            configMap.put("readMethod", choice);
            configMap.put("firstNormalForm", String.valueOf(firstNormalForm));

        }

        RDFtoCSV rdftocsv;

        if (fileURL != null && !fileURL.isEmpty()) {
            rdftocsv = new RDFtoCSV(fileURL, configMap);
        } else {
            assert input != null;
            System.out.println("input.getAbsolutePath() = " + input.getAbsolutePath());
            rdftocsv = new RDFtoCSV(input.getAbsolutePath(), configMap);
        }

        FinalizedOutput<byte[]> zipFileInBytes = rdftocsv.convertToZip();

        return CompletableFuture.completedFuture(zipFileInBytes.getOutputData());
    }

//    public static int countFilesInZip(byte[] zippedBytes) throws IOException {
//        // Step 1: Convert the byte array into a ByteArrayInputStream
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zippedBytes);
//
//        // Step 2: Wrap the InputStream in a ZipInputStream to read the ZIP contents
//        ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
//
//        // Step 3: Initialize a counter for the number of files
//        int fileCount = 0;
//
//        // Step 4: Iterate through each entry in the ZIP file
//        ZipEntry entry;
//        while ((entry = zipInputStream.getNextEntry()) != null) {
//            // Ignore directories
//            if (!entry.isDirectory()) {
//                fileCount++;
//            }
//        }
//
//        // Step 5: Close the streams
//        zipInputStream.close();
//        byteArrayInputStream.close();
//
//        // Step 6: Return the count
//        return fileCount;
//    }

//    public static String calculateChecksum(MultipartFile file) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            InputStream fis = file.getInputStream();
//
//            byte[] byteArray = new byte[1024];
//            int bytesCount;
//
//            while ((bytesCount = fis.read(byteArray)) != -1) {
//                digest.update(byteArray, 0, bytesCount);
//            }
//            fis.close();
//
//            byte[] bytes = digest.digest();
//            return Hex.encodeHexString(bytes);  // Return checksum as a hexadecimal string
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public boolean hasBOM(MultipartFile file) {
//        try (InputStream inputStream = file.getInputStream()) {
//            byte[] bom = new byte[3];
//            inputStream.read(bom);
//            return (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean validateFileContent(MultipartFile file) {
//        try {
//            String content = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);
//            // Perform checks for unexpected characters (e.g., unexpected null characters, BOM, etc.)
//            // Check for null characters
//            return !content.contains("\u0000");// Add other validation checks here
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    /**
     * Save MultipartFile.
     *
     * @param multipartFile the multipart file
     * @return the file
     * @throws IOException the io exception
     */
    public File saveFile(MultipartFile multipartFile) throws IOException {
        // Create the directory if it does not exist
        Path directory = Paths.get("lib");
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            try {
                Files.createDirectories(directory); // Ensure the directory exists
                System.out.println("Directory " + directory + " has been created ");
            } catch (FileAlreadyExistsException ex) {
                // File already exists, continue.
            }
        }
        // Now create the file in the 'lib' directory
        Path filePath = directory.resolve(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (FileAlreadyExistsException | DirectoryNotEmptyException ex) {
            try (InputStream inputStreamInException = multipartFile.getInputStream()) {
                Path newDirectoryPath = adjustDirectoryPathWithRandomNumber(filePath);
                Path newFilePath = adjustFilePathWithRandomNumber(newDirectoryPath);
                Files.copy(inputStreamInException, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

        }
        return filePath.toFile();
    }

    private Path adjustFilePathWithRandomNumber(Path filePath) {
        long seed = System.currentTimeMillis(); // Seed can be system time or any other value
        // Get the original file name
        String originalFileName = filePath.toString();

        // Guard against null file name
        if (originalFileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        // Find the last dot to split the name and extension
        int dotIndex = originalFileName.lastIndexOf('.');

        String baseName;
        String extension;

        if (dotIndex == -1) {
            // If there's no extension, treat the whole thing as the base name
            baseName = originalFileName;
            extension = ""; // No extension
        } else {
            baseName = originalFileName.substring(0, dotIndex); // Get the name part
            extension = originalFileName.substring(dotIndex);   // Get the extension with the dot
        }

        // Generate a random number using a fixed seed
        Random random = new Random(seed);
        int randomNumber = random.nextInt(10000); // Generates a number between 0 and 9999

        // Construct the new file name with random number
        String newFileName = baseName + "_" + randomNumber + extension;

        // Return the modified file name
        return Path.of(newFileName);
    }


    private Path adjustDirectoryPathWithRandomNumber(Path filePath) {
        long seed = System.currentTimeMillis(); // Seed for random number generation

        // Get the original directory path
        Path parentDirectory = filePath.getParent();

        // Ensure the file resides in a directory
        if (parentDirectory == null) {
            throw new IllegalArgumentException("The file must reside in a directory and not at the root level.");
        }

        // Get the file name from the original path
        String originalFileName = filePath.getFileName().toString();

        // Generate a random number using the seed
        Random random = new Random(seed);
        int randomNumber = random.nextInt(10000); // Generates a number between 0 and 9999

        // Construct the new directory name by appending the random number to the original directory name
        String newDirectoryName = parentDirectory.getFileName().toString() + "_" + randomNumber;

        // Check if parentDirectory is the root, i.e., no grandparent directory
        Path newDirectoryPath;
        if (parentDirectory.getParent() == null) {
            // If it's at the root level, resolve the new directory from the root directly
            newDirectoryPath = parentDirectory.getRoot().resolve(newDirectoryName);
        } else {
            // Otherwise, resolve from the grandparent
            newDirectoryPath = parentDirectory.getParent().resolve(newDirectoryName);
        }

        // Construct the final file path in the new directory

        // Return the modified file path with the new directory
        return newDirectoryPath.resolve(originalFileName);
    }

//    public static boolean isFileLocked(File file) {
//        try (FileInputStream fis = new FileInputStream(file);
//             FileChannel channel = fis.getChannel();
//             FileLock lock = channel.tryLock()) {
//            return lock == null;
//        } catch (IOException e) {
//            return true;
//        }
//    }
//
//    public void transferFile(MultipartFile multipartFile) throws IOException {
//        // Use absolute path for the file storage
//        String filePath = "/app/lib/";
//
//        File directory = new File(filePath);
//        if (!directory.exists()) {
//            // Create the directory if it doesn't exist
//            directory.mkdirs();
//        }
//        // Create the full file path
//        File file = new File(directory, multipartFile.getOriginalFilename());
//
//        // Transfer the content of the MultipartFile to the file
//        multipartFile.transferTo(file);
//    }
//
//    public void ListFilesInDirectory (String directoryPath) {
//        // Create a File object for the directory
//        File directory = new File(directoryPath);
//
//        // Check if the directory exists and is a directory
//        if (directory.exists() && directory.isDirectory()) {
//            // Get the list of files and directories in the specified directory
//            File[] filesList = directory.listFiles();
//
//            if (filesList != null) {
//                // Write the file names to the output file
//                for (File file : filesList) {
//                    if (file.isDirectory()) {
//                        System.out.println("Directory: " + file.getName());
//                    } else {
//                        System.out.println("File: " + file.getName());
//                    }
//                }
//                System.out.println("List of files written");
//            } else {
//                System.out.println("The specified path is not a directory or an I/O error occurred.");
//            }
//        } else {
//            System.out.println("The specified path does not exist or is not a directory.");
//        }
//    }
//
//
//    public String getFileContent(MultipartFile file) throws IOException {
//        StringBuilder content = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                content.append(line).append(System.lineSeparator());
//            }
//        }
//        return content.toString();
//    }

    /**
     * This method returns only CSV string, no metadata, and it is not in .zip. This method is only possible with
     * RDF file URL. If multiple files are requested, it will put the CSVs into one string and
     * divide them with visible horizontal delimiter.
     *
     * @param config the AppConfig instance
     * @return csv string
     * @throws IOException the io exception
     */
    public String getCSVString(AppConfig config) throws IOException {
        System.out.println("Inside getCSVString, is config null? " + (config == null));
        System.out.println("Using config on thread " + Thread.currentThread().getName());

        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        String result = rdFtoCSV.getCSVTableAsString();
        System.out.println(result);
        return result;
    }

    /**
     * This method returns only CSV string, no metadata, and it is not in .zip. This method is possible only with
     * "basicQuery" aka one table creation. If multiple files are requested, it will pUt the CSVs into one string and
     * divide them with visible horizontal delimiter
     *
     * @param config        the AppConfig instance
     * @return csv string from file
     * @throws IOException the io exception
     */
    public String getCSVStringFromFile(AppConfig config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        String result = rdFtoCSV.getCSVTableAsString();
        System.out.println(result);
        return result;
    }

    /**
     * This method returns only CSVW Metadata string given RDF file URL, it returns no CSV, and it is not in .zip.
     *
     * @param config the AppConfig instance
     * @return metadata string
     * @throws IOException the io exception
     */
    public String getMetadataString(AppConfig config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        String result = rdFtoCSV.getMetadataAsString();
        System.out.println(result);
        return prettyPrintStringFromLibrary(result);
    }

    /**
     * This method returns only CSVW Metadata string given RDF file, it returns no CSV, and it is not in .zip.
     *
     * @param config        the AppConfig instance
     * @return metadata string from file
     * @throws IOException the io exception
     */
    public String getMetadataStringFromFile(AppConfig config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        String result = rdFtoCSV.getMetadataAsString();
        
        //System.out.println(result);
        return prettyPrintStringFromLibrary(result);
    }

//    public void getZip() throws IOException {
//        byte[] buffer = new byte[1024];
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ZipOutputStream zout = new ZipOutputStream(bos);
//
//        try {
//            zout.putNextEntry(new ZipEntry("first.pdf"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//

    /**
     * Build AppConfig instance from endpoint parameters with URL/file path.
     *
     * @param filePathOrUrl the file path or URL to the RDF file
     * @param table the table parameter - how many tables to create (ONE/MORE)
     * @param conversionMethod the conversion method parameter (RDF4J/STREAMING/BIGFILESTREAMING)
     * @param firstNormalForm  the first normal form - true if first normal form is to be activated
     * @param preferredLanguages comma-separated list of preferred language codes (e.g., "en,cs,de")
     * @param namingConvention the naming convention for CSV headers
     * @return the AppConfig instance configured with the provided parameters
     */
    public AppConfig buildAppConfig(
            String filePathOrUrl,
            String table,
            String conversionMethod,
            Boolean firstNormalForm,
            String preferredLanguages,
            String namingConvention) {

        System.out.println("Building AppConfig with parameters:");
        System.out.println("filePathOrUrl: " + filePathOrUrl);
        System.out.println("table: " + table);
        System.out.println("conversionMethod: " + conversionMethod);
        System.out.println("firstNormalForm: " + firstNormalForm);
        System.out.println("preferredLanguages: " + preferredLanguages);
        System.out.println("namingConvention: " + namingConvention);

        // Create AppConfig builder
        AppConfig.Builder builder = new AppConfig.Builder(filePathOrUrl);

        // Set table parameter (ONE/MORE)
        if (table != null && !table.equalsIgnoreCase("null")) {
            builder.multipleTables(table.equalsIgnoreCase("MORE") || table.equalsIgnoreCase("more"));
        } else {
            builder.multipleTables(false); // Default to ONE table
        }

        // Set conversion method (readMethod)
        if (conversionMethod != null && !conversionMethod.equalsIgnoreCase("null")) {
            builder.parsing(conversionMethod.toUpperCase());
        } else {
            builder.parsing(String.valueOf(ParsingChoice.RDF4J));
        }

        // Set first normal form
        if (firstNormalForm != null) {
            builder.firstNormalForm(firstNormalForm);
        } else {
            builder.firstNormalForm(false);
        }

        // Set preferred languages
        if (preferredLanguages != null && !preferredLanguages.trim().isEmpty()) {
            builder.preferredLanguages(preferredLanguages);
        }

        // Set naming convention
        if (namingConvention != null && !namingConvention.isEmpty()) {
            builder.columnNamingConvention(namingConvention);
        }

        AppConfig config = builder.build();
        System.out.println("AppConfig built successfully");
        System.out.println("Thread: " + Thread.currentThread().getName());

        return config;
    }

    /**
     * Build AppConfig instance from endpoint parameters with MultipartFile.
     *
     * @param multipartFile the multipart file containing the RDF data
     * @param table the table parameter - how many tables to create (ONE/MORE)
     * @param conversionMethod the conversion method parameter (RDF4J/STREAMING/BIGFILESTREAMING)
     * @param firstNormalForm  the first normal form - true if first normal form is to be activated
     * @param preferredLanguages comma-separated list of preferred language codes (e.g., "en,cs,de")
     * @param namingConvention the naming convention for CSV headers
     * @return the AppConfig instance configured with the provided parameters
     * @throws IOException if file saving fails
     */
    public AppConfig buildAppConfig (
            MultipartFile multipartFile,
            String table,
            String conversionMethod,
            Boolean firstNormalForm,
            String preferredLanguages,
            String namingConvention) throws IOException {

        // Save the multipart file to local filesystem
        File input = saveFile(multipartFile);
        
        // Use the overloaded method with the file path
        return buildAppConfig(
                input.getAbsolutePath(),
                table,
                conversionMethod,
                firstNormalForm,
                preferredLanguages,
                namingConvention
        );
    }

    /**
     * Prepare config parameter map to initialize RDFtoCSV from the depended on library.
     * @deprecated Use {@link #buildAppConfig(String, String, Boolean, String, String)} instead
     *
     * @param table            the table parameter (ONE/MORE)
     * @param conversionMethod the conversion method parameter (RDF4J/STREAMING/BIGFILESTREAMING)
     * @param firstNormalForm  the first normal form - true if first normal form is to be activated
     * @param preferredLanguages comma-separated list of preferred language codes (e.g., "en,cs,de")
     * @param namingConvention the naming convention for CSV headers
     * @return the configuration map of parameters for the conversion
     */
    @Deprecated
    public CompletableFuture<Map<String, String>> prepareConfigParameter(
            String table, 
            String conversionMethod, 
            Boolean firstNormalForm,
            String preferredLanguages,
            String namingConvention) {
        
        System.out.println("conversionMethod: " + conversionMethod);
        System.out.println("preferredLanguages: " + preferredLanguages);
        System.out.println("namingConvention: " + namingConvention);
        
        // Prepare map for config parameters
        Map<String, String> config = new HashMap<>();
        
        // Table parameter
        if (table != null && !table.equalsIgnoreCase("null")) {
            config.put("table", table);
        } else {
            config.put("table", String.valueOf(TableChoice.ONE));
        }
        
        // Conversion method parameter
        if (conversionMethod != null && !conversionMethod.equalsIgnoreCase("null")) {
            config.put("readMethod", conversionMethod);
        } else {
            config.put("readMethod", String.valueOf(ParsingChoice.RDF4J));
        }
        
        // First normal form parameter
        if (firstNormalForm != null) {
            config.put("firstNormalForm", String.valueOf(firstNormalForm));
        } else {
            config.put("firstNormalForm", "false");
        }
        
        // Preferred languages parameter
        if (preferredLanguages != null && !preferredLanguages.trim().isEmpty()) {
            config.put("preferredLanguages", preferredLanguages);
        }
        
        // Naming convention parameter
        if (namingConvention != null && !namingConvention.isEmpty()) {
            config.put("namingConvention", namingConvention);
        }

        System.out.println("Set configMap to: \n");
        System.out.println("table: " + config.get("table"));
        System.out.println("readMethod: " + config.get("readMethod"));
        System.out.println("firstNormalForm: " + config.get("firstNormalForm"));
        System.out.println("preferredLanguages: " + config.get("preferredLanguages"));
        System.out.println("namingConvention: " + config.get("namingConvention"));

        System.out.println("Thread: " + Thread.currentThread().getName());
        System.out.println("Creating config: " + System.identityHashCode(config));
        System.out.println("Config values: " + config);

        return CompletableFuture.completedFuture(config);
    }

    /**
     * Get csv file from url byte [ ].
     *
     * @param config the AppConfig instance
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public byte[] getCSVFileFromURL(AppConfig config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        return rdFtoCSV.getCSVTableAsFile().getOutputData();
    }

    /**
     * Get csv file from file byte [ ].
     *
     * @param config        the AppConfig instance
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public byte[] getCSVFileFromFile(AppConfig config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        return rdFtoCSV.getCSVTableAsFile().getOutputData();
    }

    /**
     * Get metadata file from url byte [ ].
     *
     * @param config the AppConfig instance
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public byte[] getMetadataFileFromURL(AppConfig config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        return rdFtoCSV.getMetadataAsFile().getOutputData();
    }

    /**
     * Get metadata file from file byte [ ].
     *
     * @param config        the AppConfig instance
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public byte[] getMetadataFileFromFile(AppConfig config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
        return rdFtoCSV.getMetadataAsFile().getOutputData();
    }

    /**
     * Makes JSON string from plain string to pretty print JSON string.
     * @param jsonStringNotPretty String of JSON, that is not prettified
     * @return Prettified JSON String
     */
    public String prettyPrintStringFromLibrary(String jsonStringNotPretty) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Object json = gson.fromJson(jsonStringNotPretty, Object.class);
        return gson.toJson(json);
    }
}
