package org.rdftocsvconverter.RDFtoCSVW.service;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.rdftocsvconverter.RDFtoCSVW.api.model.RDFtoCSVW;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.junit.Assert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Service
public class RDFtoCSVWService {
    public File getCSVW(MultipartFile multipartFile, String delimiter, String filename) {
        File file = new File("src/main/resources/targetFile.tmp");
        File output = new File("src/main/resources/example.csv");
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        return  output;
    }
}
