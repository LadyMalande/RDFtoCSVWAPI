package org.rdftocsvconverter.RDFtoCSVW.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Test WebConfig is loading.
 */
class WebConfigTest {

    //BaseRock generated method id: ${testCorsConfigurer}, hash: 6CE756B18D7190A0D5C51A1AC7AC5492
    @Test
    void testCorsConfigurerNotNull() {
        WebConfig webConfig = new WebConfig();
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        assertNotNull(configurer);

    }

    //BaseRock generated method id: ${testWebConfigCreation}, hash: E2BF788E8AB6F392C39024A358F112A1
    @Test
    void testWebConfigCreation() {
        WebConfig webConfig = new WebConfig();
        assertNotNull(webConfig);
    }
}