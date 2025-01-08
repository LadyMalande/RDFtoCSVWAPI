package org.rdftocsvconverter.RDFtoCSVW;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.rdftocsvconverter.RDFtoCSVW.api.controller.RDFtoCSVWController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The Context is creating controller test.
 */
@SpringBootTest
class ContextIsCreatingControllerTest {

    @Autowired
    private RDFtoCSVWController controller;

    /**
     * Context loads and creates Controller.
     *
     */
    @Test
    void contextLoads(){
        assertThat(controller).isNotNull();
    }
}

