package org.rdftocsvconverter.RDFtoCSVW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for RDF to CSVW API.
 * Provides RESTful endpoints for converting RDF data to CSV on the Web (CSVW) format.
 * 
 * Features:
 * - Asynchronous processing for long-running conversions
 * - Redis caching for async task management
 * - Multiple conversion methods (RDF4J, STREAMING, BIGFILESTREAMING)
 * - Support for file upload and URL-based RDF sources
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
public class RDFToCSVWApiApplication {

	/**
	 * The entry point of the application.
	 * Starts the Spring Boot application and initializes all configured beans and services.
	 *
	 * @param args command line arguments passed to the application
	 */
	public static void main(String[] args) {

		SpringApplication.run(RDFToCSVWApiApplication.class, args);
	}
/* 
	@Bean
	public CommandLineRunner appRunner(RDFtoCSVWService rDFtoCSVWService) {
		return new AppRunner(rDFtoCSVWService);
	}

	
	@Bean
	public CommandLineRunner appRunner(GithubLookupService RDFToCSVWApiApplication) {
		return new AppRunner(gitHubLookupService);
	}
*/
/*	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("GithubLookup-");
		executor.initialize();
		return executor;
	}*/

}
