package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import org.rdftocsvconverter.RDFtoCSVW.service.ExpensiveComputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compute")
public class ComputationController {

    private static final Logger logger = LoggerFactory.getLogger(ComputationController.class);
    private final ExpensiveComputationService computationService;

    public ComputationController(ExpensiveComputationService computationService) {
        this.computationService = computationService;
        logger.info("ComputationController initialized with computation service");
    }


    @GetMapping("/testlogging")
    public String testLogging() {
        logger.trace("This is TRACE level");
        logger.debug("This is DEBUG level");
        logger.info("This is INFO level");
        logger.warn("This is WARN level");
        logger.error("This is ERROR level");
        return "Check your logs";
    }

    @GetMapping("/{input}")
    public String compute(@PathVariable String input) {
        logger.debug("Received computation request for input: {}", input);
        long startTime = System.currentTimeMillis();

        try {
            String result = computationService.expensiveComputation(input);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully computed result for input '{}' in {} ms", input, duration);
            logger.debug("Computation result for '{}': {}", input, result);

            return result;
        } catch (Exception e) {
            logger.error("Error processing computation for input '{}': {}", input, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/clear/{input}")
    public String clearCache(@PathVariable String input) {
        logger.debug("Received cache clear request for input: {}", input);

        try {
            computationService.clearCache(input);
            logger.info("Successfully cleared cache for input: {}", input);
            return "Cache cleared for " + input;
        } catch (Exception e) {
            logger.error("Failed to clear cache for input '{}': {}", input, e.getMessage(), e);
            throw e;
        }
    }
}
