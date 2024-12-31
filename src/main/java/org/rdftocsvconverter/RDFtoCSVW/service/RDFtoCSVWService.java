package org.rdftocsvconverter.RDFtoCSVW.service;

import com.miklosova.rdftocsvw.convertor.RDFtoCSV;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import org.apache.commons.codec.binary.Hex;

import org.apache.commons.io.IOUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Service
public class RDFtoCSVWService {
    public byte[] getCSVW(MultipartFile multipartFile, String fileURL, String choice, String table, Boolean firstNormalForm) throws IOException {
        File input = null;
        if(multipartFile != null) {
            System.out.println("multipartFile != null ");
            input = saveFile(multipartFile);
        }

        try {
            Class<?> clazz = Class.forName("org.eclipse.rdf4j.rio.nquads.NQuadsParserFactory");
            System.out.println("Class loaded: " + clazz.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }
        /*
        System.out.println("C---------- ----------------- " );
        System.out.println(System.getProperty("java.class.path"));
        System.out.println("Content of the multipart file validation: " + validateFileContent(multipartFile));
        System.out.println("Content of the multipart file has BOM: " + hasBOM(multipartFile));
        System.out.println("Content of the multipart file CHECKSUM: " + calculateChecksum(multipartFile));


        System.out.println(getFileContent(multipartFile));
        System.out.println("Copied incoming multipart file to " + input.getAbsolutePath());
        System.out.println("C---------- ----------------- ");
        System.out.println("input.getPath() = " + input.getPath());
        System.out.println("input.getAbsolutePath() = " + input.getAbsolutePath());
        System.out.println("input.getCanonicalPath() = " + input.getCanonicalPath());
        System.out.println("input.getName() = " + input.getName());
        //System.out.println("lib.getAbsolutePath() = " + lib.getAbsolutePath());
        System.out.println("multipartFile.getContentType() = " + multipartFile.getContentType());
        //ListFilesInDirectory(lib.getAbsolutePath());


         */

        Map<String, String> configMap = null;
        if(table != null){
            configMap = new HashMap<>();
            configMap.put("table", table);
            configMap.put("readMethod", choice);
            configMap.put("firstNormalForm", String.valueOf(firstNormalForm));

        }

        //RDFtoCSV rdftocsv = new RDFtoCSV(input.getAbsolutePath(), configMap);
        RDFtoCSV rdftocsv;

        if(!fileURL.isEmpty()){
            rdftocsv = new RDFtoCSV(fileURL, configMap);
        } else{
            System.out.println("input.getAbsolutePath() = " + input.getAbsolutePath());
            rdftocsv = new RDFtoCSV(input.getAbsolutePath(), configMap);
        }

        FinalizedOutput<byte[]> zipFileInBytes = rdftocsv.convertToZip();

        //return zipFileInBytes.getOutputData();

        //return byteArrayOutputStream.toByteArray();
        return zipFileInBytes.getOutputData();



    }

    public static int countFilesInZip(byte[] zippedBytes) throws IOException {
        // Step 1: Convert the byte array into a ByteArrayInputStream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zippedBytes);

        // Step 2: Wrap the InputStream in a ZipInputStream to read the ZIP contents
        ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

        // Step 3: Initialize a counter for the number of files
        int fileCount = 0;

        // Step 4: Iterate through each entry in the ZIP file
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            // Ignore directories
            if (!entry.isDirectory()) {
                fileCount++;
            }
        }

        // Step 5: Close the streams
        zipInputStream.close();
        byteArrayInputStream.close();

        // Step 6: Return the count
        return fileCount;
    }

    public static String calculateChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream fis = file.getInputStream();

            byte[] byteArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();

            byte[] bytes = digest.digest();
            return Hex.encodeHexString(bytes);  // Return checksum as a hexadecimal string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasBOM(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bom = new byte[3];
            inputStream.read(bom);
            return (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateFileContent(MultipartFile file) {
        try {
            String content = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);
            // Perform checks for unexpected characters (e.g., unexpected null characters, BOM, etc.)
            // Check for null characters
            return !content.contains("\u0000");// Add other validation checks here
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File saveFile(MultipartFile multipartFile) throws IOException {
        // Create the directory if it does not exist
        Path directory = Paths.get("lib");
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            try {
                Files.createDirectories(directory); // Ensure the directory exists
                System.out.println("Directory " + directory + " has been created ");
            } catch(FileAlreadyExistsException ex) {
                // File already exists, continue.
            }
        }
        /*
        File directory = new File("lib");
        if (!directory.exists()) {
            System.out.println("Directory " + directory.getAbsolutePath() + " does not exist ");
            directory.mkdir();  // Create the 'lib' directory
        }

         */

        // Now create the file in the 'lib' directory
        //File file = new File(directory, multipartFile.getOriginalFilename());
        Path filePath = directory.resolve(multipartFile.getOriginalFilename());
        try (InputStream inputStream = multipartFile.getInputStream()) {
            /*
            if (file.exists()) {
                // Delete the existing file before copying
                file.delete();
            }

             */
            //Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (FileAlreadyExistsException | DirectoryNotEmptyException ex){
            try (InputStream inputStreamInException = multipartFile.getInputStream()) {
                Path newDirectoryPath = adjustDirectoryPathWithRandomNumber(filePath);
                Path newFilePath = adjustFilePathWithRandomNumber(newDirectoryPath);
                Files.copy(inputStreamInException, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

        }
        //multipartFile.transferTo(file);  // Save the file
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
        Path newFilePath = newDirectoryPath.resolve(originalFileName);

        // Return the modified file path with the new directory
        return newFilePath;
    }

    public static boolean isFileLocked(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel();
             FileLock lock = channel.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

    public void transferFile(MultipartFile multipartFile) throws IOException {
        // Use absolute path for the file storage
        String filePath = "/app/lib/";

        File directory = new File(filePath);
        if (!directory.exists()) {
            // Create the directory if it doesn't exist
            directory.mkdirs();
        }

        // Create the full file path
        File file = new File(directory, multipartFile.getOriginalFilename());

        // Transfer the content of the MultipartFile to the file
        multipartFile.transferTo(file);
    }

    public void ListFilesInDirectory (String directoryPath) {
        // Create a File object for the directory
        File directory = new File(directoryPath);

        // Check if the directory exists and is a directory
        if (directory.exists() && directory.isDirectory()) {
            // Get the list of files and directories in the specified directory
            File[] filesList = directory.listFiles();

            if (filesList != null) {
                // Write the file names to the output file
                for (File file : filesList) {
                    if (file.isDirectory()) {
                        System.out.println("Directory: " + file.getName());
                    } else {
                        System.out.println("File: " + file.getName());
                    }
                }
                System.out.println("List of files written");
            } else {
                System.out.println("The specified path is not a directory or an I/O error occurred.");
            }
        } else {
            System.out.println("The specified path does not exist or is not a directory.");
        }
    }


    public String getFileContent(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }

    /**
     * This method returns only CSV string, no metadata and it is not in .zip. This method is only possible with
     * RDF file URL. If multiple files are requested, it will pUt the csvs into one string and
     * divide them with visible horizontal delimiter.
     * @param url
     * @param config
     * @return
     * @throws IOException
     */
    public String getCSVString(String url, Map<String, String> config) throws IOException {

        RDFtoCSV rdFtoCSV = new RDFtoCSV(url, config);
        String result = rdFtoCSV.getCSVTableAsString();
        System.out.println(result);
        return result;
    }

    /**
     * This method returns only CSV string, no metadata and it is not in .zip. This method is possible only with
     * "basicQuery" aka one table creation. If multiple files are requested, it will pUt the csvs into one string and
     * divide them with visible horizontal delimiter
     * @param multipartFile
     * @param config
     * @return
     * @throws IOException
     */
    public String getCSVStringFromFile(MultipartFile multipartFile, Map<String, String> config) throws IOException {

        File input = saveFile(multipartFile);

        RDFtoCSV rdFtoCSV = new RDFtoCSV(input.getAbsolutePath(), config);
        String result = rdFtoCSV.getCSVTableAsString();
        System.out.println(result);
        return result;
    }

    /**
     * This method returns only CSVW Metadata string, no CSV and it is not in .zip.
     * @param url
     * @param config
     * @return
     * @throws IOException
     */
    public String getMetadataString(String url, Map<String, String> config) throws IOException {

        RDFtoCSV rdFtoCSV = new RDFtoCSV(url, config);
        String result = rdFtoCSV.getMetadataAsString();
        System.out.println(result);
        return result;
    }

    /**
     * This method returns only CSVW Metadata string, no CSV and it is not in .zip.
     * @param multipartFile
     * @param config
     * @return
     * @throws IOException
     */
    public String getMetadataStringFromFile(MultipartFile multipartFile, Map<String, String> config) throws IOException {

        File input = saveFile(multipartFile);

        RDFtoCSV rdFtoCSV = new RDFtoCSV(input.getAbsolutePath(), config);
        String result = rdFtoCSV.getMetadataAsString();
        System.out.println(result);
        return result;
    }

    public void getZip() throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bos);

        try {
            zout.putNextEntry(new ZipEntry("first.pdf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int length;
        File file2 = new File("src/main/resources/example.csv");
        FileInputStream fileInputStream = new FileInputStream(file2);
        System.out.println("before while cycle");
        while((length = fileInputStream.read(buffer)) > 0)
        {
            System.out.println("In while cycle writing");
            zout.write(buffer, 0, length);
        }
        zout.flush();
        zout.finish();
        bos.flush();
        zout.close();
        bos.close();


    }


    public Map<String, String> prepareConfigParameter(String table, String conversionMethod, Boolean firstNormalForm) {

        // Prepare map for config parameters
        Map<String, String> config = new HashMap<>();
        // Log optional parameters if they are present
        if (table != null) config.put("table", table);
        if (conversionMethod != null) config.put("readMethod", conversionMethod);
        if (firstNormalForm != null) config.put("firstNormalForm", String.valueOf(firstNormalForm));

        return config;
    }

    public byte[] getCSVFileFromURL(String url, Map<String, String> config) throws IOException {
        RDFtoCSV rdFtoCSV = new RDFtoCSV(url, config);
        return rdFtoCSV.getCSVTableAsFile().getOutputData();
    }

    public byte[] getCSVFileFromFile(MultipartFile multipartFile, Map<String, String> config) throws IOException {
        File input = saveFile(multipartFile);

        RDFtoCSV rdFtoCSV = new RDFtoCSV(input.getAbsolutePath(), config);
        return rdFtoCSV.getCSVTableAsFile().getOutputData();
    }

    public byte[] getMetadataFileFromURL(String url, Map<String, String> config) throws IOException {
        System.out.println("url " + url  + config.toString());
        RDFtoCSV rdFtoCSV = new RDFtoCSV(url, config);
        return rdFtoCSV.getMetadataAsFile().getOutputData();
    }

    public byte[] getMetadataFileFromFile(MultipartFile multipartFile, Map<String, String> config) throws IOException {
        File input = saveFile(multipartFile);

        RDFtoCSV rdFtoCSV = new RDFtoCSV(input.getAbsolutePath(), config);
        return rdFtoCSV.getMetadataAsFile().getOutputData();
    }
}
