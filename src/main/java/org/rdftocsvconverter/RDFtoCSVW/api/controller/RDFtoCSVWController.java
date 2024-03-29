package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.rdftocsvconverter.RDFtoCSVW.api.model.RDFtoCSVW;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class RDFtoCSVWController {

    private RDFtoCSVWService rdFtoCSVWService;

    @Autowired
    public RDFtoCSVWController(RDFtoCSVWService rdFtoCSVWService){
        this.rdFtoCSVWService = rdFtoCSVWService;
    }

    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    @PostMapping("/rdftocsvw")
    public byte[] getCSVW(@RequestParam MultipartFile file, @RequestParam String delimiter, @RequestParam String filename){
        try {
            return rdFtoCSVWService.getCSVW(file, delimiter, filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    @PostMapping("/getcsvstring")
    public String getCSVString(@RequestParam MultipartFile file, @RequestParam String delimiter, @RequestParam String filename){
        try {
            return rdFtoCSVWService.getCSVString(file, delimiter, filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    @GetMapping("/getZip")
    public void getZip(HttpServletResponse response){
        System.out.println("Beginning of getZip path Controller");
        try {
           rdFtoCSVWService.getZip();
            System.out.println("In try getZip path Controller");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/rdftocsvw-javaconfig")
	public byte[] getCSVWWithConfig(@RequestParam MultipartFile file, @RequestParam String delimiter, @RequestParam String filename){
        try {
            return rdFtoCSVWService.getCSVW(file, delimiter, filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    @RequestMapping(value = "/zip", produces="application/zip")
    public byte[] zipFiles(@RequestParam MultipartFile file, @RequestParam String delimiter, @RequestParam String filename, HttpServletResponse response) throws IOException {
        // Setting HTTP headers
        response.addHeader("Content-Disposition", "attachment; filename=\"test.zip\"");

        // Creating byteArray stream, make it bufferable and passing this buffer to ZipOutputStream
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

        Path path = Paths.get("src/main/resources/example.zip");
        Files.write(path, byteArrayOutputStream.toByteArray());

        return byteArrayOutputStream.toByteArray();
    }
}
