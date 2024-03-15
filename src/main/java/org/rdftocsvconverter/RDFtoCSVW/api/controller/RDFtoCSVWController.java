package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import org.rdftocsvconverter.RDFtoCSVW.api.model.RDFtoCSVW;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
public class RDFtoCSVWController {

    private RDFtoCSVWService rdFtoCSVWService;

    @Autowired
    public RDFtoCSVWController(RDFtoCSVWService rdFtoCSVWService){
        this.rdFtoCSVWService = rdFtoCSVWService;
    }

    @CrossOrigin(origins = "http://localhost:4000")
    @PostMapping("/rdftocsvw")
    public File getCSVW(@RequestParam MultipartFile file, @RequestParam String delimiter, @RequestParam String filename){
        return rdFtoCSVWService.getCSVW(file, delimiter, filename);
    }

    @GetMapping("/rdftocsvw-javaconfig")
	public File getCSVWWithConfig(@RequestParam MultipartFile file, @RequestParam String delimiter, @RequestParam String filename){
        return rdFtoCSVWService.getCSVW(file, delimiter, filename);
    }
}
