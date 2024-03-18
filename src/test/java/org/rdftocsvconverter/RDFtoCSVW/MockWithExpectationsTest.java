package org.rdftocsvconverter.RDFtoCSVW;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.rdftocsvconverter.RDFtoCSVW.api.controller.RDFtoCSVWController;
import org.rdftocsvconverter.RDFtoCSVW.service.RDFtoCSVWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;

@WebMvcTest(RDFtoCSVWController.class)
class MockWithExpectationsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RDFtoCSVWService service;

    @Autowired
    ResourceLoader resourceLoader;

    @Test
    void greetingShouldReturnMessageFromService() throws Exception {

        File file = new File("src/main/resources/test.zip");
        LinkedMultiValueMap<String, File> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("file", file);
        //when(service.getZip()).thenAnswer();
        this.mockMvc.perform(get("/getZip")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void postZip() throws Exception {


        File fn = new File("resources/database.db");
        System.out.println(fn.getAbsolutePath());
        Resource fileResource = new ClassPathResource("typy-pracovních-vztahů.ttl");
        System.out.println(fileResource.getFilename());
        assertNotNull(fileResource);

        MockMultipartFile firstFile = new MockMultipartFile(
                "file",fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream());

        assertNotNull(firstFile);

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("delimiter", ";");
        params.add("filename", "newfile.csv");

        this.mockMvc.perform(MockMvcRequestBuilders
                .multipart("/rdftocsvw")
           .file(firstFile).params(params))
            .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void getCSVString() throws Exception {
        Resource fileResource = new ClassPathResource("typy-pracovních-vztahů.ttl", this.getClass().getClassLoader());
        Resource fileResource2 = resourceLoader.getResource("classpath:/typy-pracovních-vztahů.ttl");
        System.out.println(fileResource2.getFilename());
        assertNotNull(resourceLoader.getResource("classpath:/typy-pracovních-vztahů.ttl"));

        MockMultipartFile firstFile = new MockMultipartFile(
                "file",fileResource2.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource2.getInputStream());
        // create MultipartFile

        System.out.println(service.getCSVString(firstFile,";","newcsvfile"));


        assertNotNull(firstFile);

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("delimiter", ";");
        params.add("filename", "newfile");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/getcsvstring")
                        .file(firstFile).params(params))
                .andDo(print())
                .andExpect(status().isOk()).andExpect(content().string("nothing"))
                .andReturn();
    }
}
