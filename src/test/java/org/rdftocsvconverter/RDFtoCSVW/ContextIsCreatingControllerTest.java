package org.rdftocsvconverter.RDFtoCSVW;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.rdftocsvconverter.RDFtoCSVW.api.controller.RDFtoCSVWController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ContextIsCreatingControllerTest {

    @Autowired
    private RDFtoCSVWController controller;

    @Test
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }
}

