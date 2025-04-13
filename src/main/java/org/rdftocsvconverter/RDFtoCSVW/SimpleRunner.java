package org.rdftocsvconverter.RDFtoCSVW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class SimpleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SimpleRunner.class);

    @Override
    public void run(String... args) throws Exception {
        log.error(">>> org.rdftocsvconverter.RDFtoCSVW.SimpleRunner ran!");
    }
}
