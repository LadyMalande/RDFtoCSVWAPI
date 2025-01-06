package org.rdftocsvconverter.RDFtoCSVW;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RDFToCSVWApiApplicationTest {

    @Autowired
    private RDFToCSVWApiApplication app;

    @Test
    void appStarts() throws Exception {
        assertThat(app).isNotNull();
    }
}
