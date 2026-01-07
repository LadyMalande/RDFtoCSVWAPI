package org.rdftocsvconverter.RDFtoCSVW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The RDFtoCSVWAPI application.
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
public class RDFToCSVWApiApplication {

	/**
	 * The entry point of application.
	 *
	 * @param args the input arguments
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
