package org.rdftocsvconverter.RDFtoCSVW.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Test WebConfig is loading.
 */
class WebConfigTest {

    @Test
    void testCorsConfigurerNotNull() {
        WebConfig webConfig = new WebConfig();
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        assertNotNull(configurer);
    }

    @Test
    void testWebConfigCreation() {
        WebConfig webConfig = new WebConfig();
        assertNotNull(webConfig);
    }

    @Test
    void testCorsConfigurerBeanCreation() {
        // Given
        WebConfig webConfig = new WebConfig();

        // When
        WebMvcConfigurer configurer = webConfig.corsConfigurer();

        // Then
        assertNotNull(configurer);
    }

    @Test
    void testCorsConfigurerIsNotNull() {
        // Given
        WebConfig config = new WebConfig();

        // When
        WebMvcConfigurer result = config.corsConfigurer();

        // Then
        assertNotNull(result, "CORS configurer should not be null");
    }
}