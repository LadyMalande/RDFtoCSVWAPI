package org.rdftocsvconverter.RDFtoCSVW.service;



import com.miklosova.rdftocsvw.convertor.CSVTableCreator;
import com.miklosova.rdftocsvw.convertor.RDFtoCSV;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.Assert;
import com.miklosova.rdftocsvw.support.Main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Service
public class RDFtoCSVWService {
    public byte[] getCSVW(MultipartFile multipartFile, String fileURL, String choice) throws IOException {
        File file = new File("src/main/resources/targetFile.tmp");
        //File input = new File("src/main/resources/" + multipartFile.getOriginalFilename());
        File input = new File(multipartFile.getOriginalFilename());
        File output = new File("src/main/resources/" + "output.csv");

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
            os.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (OutputStream os = new FileOutputStream(input)) {
            os.write(multipartFile.getBytes());
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        System.out.println("C---------- ----------------- " );
        System.out.println(getFileContent(multipartFile));
        System.out.println("Copied incoming multipart file to " + input.getAbsolutePath());
        System.out.println("C---------- ----------------- ");

        Map<String, String> configMap = new HashMap<>();
        configMap.put("choice", choice);
        //RDFtoCSV rdftocsv = new RDFtoCSV(input.getAbsolutePath(), configMap);
        RDFtoCSV rdftocsv = new RDFtoCSV(input.getCanonicalPath(), configMap);
        FinalizedOutput<byte[]> zipFileInBytes = rdftocsv.convertToZip();

        //return zipFileInBytes.getOutputData();

        //return byteArrayOutputStream.toByteArray();
        return zipFileInBytes.getOutputData();



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
