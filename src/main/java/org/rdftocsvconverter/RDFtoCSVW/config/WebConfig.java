package org.rdftocsvconverter.RDFtoCSVW.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for handling Cross-Origin Resource Sharing (CORS).
 * Configures which origins are allowed to access the API endpoints.
 */
@Configuration
public class WebConfig {

    /**
     * Creates a CORS configurer to allow requests from specific origins.
     * Currently allows requests from:
     * - http://localhost:4000 (local development)
     * - https://ladymalande.github.io (production frontend)
     * 
     * Permits all standard HTTP methods and headers.
     *
     * @return the configured WebMvcConfigurer with CORS settings
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4000", "https://ladymalande.github.io")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}