package org.rdftocsvconverter.RDFtoCSVW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The RDFtoCSVWAPI application.
 */
@SpringBootApplication
public class RDFToCSVWApiApplication {

	/**
	 * The entry point of application.
	 *
	 * @param args the input arguments
	 */
	public static void main(String[] args) {

		SpringApplication.run(RDFToCSVWApiApplication.class, args);
	}

}
