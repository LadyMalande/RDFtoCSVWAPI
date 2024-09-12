package org.rdftocsvconverter.RDFtoCSVW.service;



import com.miklosova.rdftocsvw.convertor.CSVTableCreator;
import com.miklosova.rdftocsvw.convertor.RDFtoCSV;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import org.apache.commons.io.IOUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.Assert;
import com.miklosova.rdftocsvw.support.Main;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Service
public class RDFtoCSVWService {
    public byte[] getCSVW(MultipartFile multipartFile, String fileURL, String choice) throws IOException {
        File file = new File("src/main/resources/targetFile.tmp");
        //File input = new File("src/main/resources/" + multipartFile.getOriginalFilename());
        File fileRelative = new File(multipartFile.getOriginalFilename());

        //File lib = new File("lib/");
        File output = new File("src/main/resources/" + "output.csv");
/*
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
            os.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

 */
/*
        System.out.println("fileRelative.getAbsolutePath() = " + fileRelative.getAbsolutePath());
        try (OutputStream os = new FileOutputStream(fileRelative)) {

            os.write(multipartFile.getBytes());
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        File input = saveFile(multipartFile);
        /*
        try (OutputStream os = new FileOutputStream(saveFile(multipartFile))) {
            os.write(multipartFile.getBytes());
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

         */
        //File input = new File("lib/" + multipartFile.getOriginalFilename());
        //transferFile(multipartFile);

  /*
        Path filePath = Path.of("src/main/resources/example.csv");
        try {
            String content = Files.readString(filePath);
            assertEquals(content, "example;csv;file");
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Files.copy(file.toPath(), output.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

        // Simple file list, just for tests
        ArrayList<File> files = new ArrayList<>(2);
        File file1 = new File("src/main/resources/targetFile.tmp");
        File file2 = new File("src/main/resources/example.csv");
        files.add(file1);
        files.add(file2);

    // Packing files
    for (File fileVar : files) {
        // New zip entry and copying InputStream with file to ZipOutputStream, after all closing streams
        zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fileInputStream = new FileInputStream(fileVar);

        IOUtils.copy(fileInputStream, zipOutputStream);

        fileInputStream.close();
        zipOutputStream.closeEntry();
    }

    if (zipOutputStream != null) {
        zipOutputStream.finish();
        zipOutputStream.flush();
        IOUtils.closeQuietly(zipOutputStream);
    }
    IOUtils.closeQuietly(bufferedOutputStream);
    IOUtils.closeQuietly(byteArrayOutputStream);


    System.out.println("at the end of rdftocsvw");
    Path path = Paths.get("src/main/resources/" + filename);
    Files.write(path, byteArrayOutputStream.toByteArray());



        File fileThatCame = new File("src/main/resources/tempFileForMultipart.tmp");

        try (OutputStream os = new FileOutputStream(fileThatCame)) {
            os.write(multipartFile.getBytes());
            os.flush();
        }
        String[] params = new String[3];
        params[0] = "src/main/resources/tempFileForMultipart.tmp";
        params[1] = delimiter;
        params[2] = filename;
        CSVTableCreator ctc = new CSVTableCreator(delimiter, filename, "src/main/resources/" + multipartFile.getName());
        String result = ctc.getCSVTableAsString();
*/
        try {
            Class<?> clazz = Class.forName("org.eclipse.rdf4j.rio.nquads.NQuadsParserFactory");
            System.out.println("Class loaded: " + clazz.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }
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




        Map<String, String> configMap = new HashMap<>();
        configMap.put("choice", choice);
        //RDFtoCSV rdftocsv = new RDFtoCSV(input.getAbsolutePath(), configMap);
        RDFtoCSV rdftocsv = new RDFtoCSV(input.getAbsolutePath(), configMap);
        FinalizedOutput<byte[]> zipFileInBytes = rdftocsv.convertToZip();

        //return zipFileInBytes.getOutputData();

        //return byteArrayOutputStream.toByteArray();
        return zipFileInBytes.getOutputData();



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
            String content = IOUtils.toString(file.getInputStream(), "UTF-8");
            // Perform checks for unexpected characters (e.g., unexpected null characters, BOM, etc.)
            if (content.contains("\u0000")) { // Check for null characters
                return false;
            }
            return true; // Add other validation checks here
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File saveFile(MultipartFile multipartFile) throws IOException {
        // Create the directory if it does not exist
        Path directory = Paths.get("lib");
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            Files.createDirectories(directory); // Ensure the directory exists
            System.out.println("Directory " + directory + " has been created ");

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

        } catch (FileAlreadyExistsException ex){
            try (InputStream inputStreamInException = multipartFile.getInputStream()) {
                Path newFilePath = adjustFilePathWithRandomNumber(filePath);
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

    public String getCSVString(MultipartFile multipartFile, String delimiter, String filename) throws IOException {

        File input = new File("src/main/resources/" + multipartFile.getName());
        File output = new File("src/main/resources/" + filename);

        try (OutputStream os = new FileOutputStream(input)) {
            os.write(multipartFile.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("at the end of rdftocsvw");

        File fileThatCame = new File("src/main/resources/tempFileForMultipart.tmp");

        try (OutputStream os = new FileOutputStream(fileThatCame)) {
            os.write(multipartFile.getBytes());
            os.flush();
        }
        String[] params = new String[3];
        params[0] = "src/main/resources/" + multipartFile.getName();
        params[1] = delimiter;
        params[2] = filename;
        CSVTableCreator ctc = new CSVTableCreator(delimiter, filename, params[0]);
        String result = ctc.getCSVTableAsString();
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



}
