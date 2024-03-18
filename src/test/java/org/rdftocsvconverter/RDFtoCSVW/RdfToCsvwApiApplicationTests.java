package org.rdftocsvconverter.RDFtoCSVW;

import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.api.controller.RDFtoCSVWController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RdfToCsvwApiApplicationTests {


	@Autowired
	private RDFtoCSVWController controller;

	@Test
	void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

	@Test
	void contextDoesntLoad() throws Exception {
		assertThat(controller).isNull();
	}

}
