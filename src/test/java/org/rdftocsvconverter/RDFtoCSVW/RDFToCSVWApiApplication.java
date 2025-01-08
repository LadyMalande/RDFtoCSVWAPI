package org.rdftocsvconverter.RDFtoCSVW;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that created application is not null.
 */
@SpringBootTest
class RDFToCSVWApiApplicationTest {

    @Autowired
    private RDFToCSVWApiApplication app;

    /**
     * App starts.
     *
     */
    @Test
    void appStarts() {
        assertThat(app).isNotNull();
    }
}
