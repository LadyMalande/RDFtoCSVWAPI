package org.rdftocsvconverter.RDFtoCSVW.testingasz;

import org.rdftocsvconverter.RDFtoCSVW.SimpleRunner;
import org.rdftocsvconverter.RDFtoCSVW.service.GithubLookupService;
import org.rdftocsvconverter.RDFtoCSVW.service.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.CompletableFuture;

//@Component
public class AppRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);

    private final GithubLookupService gitHubLookupService;

    public AppRunner(GithubLookupService gitHubLookupService) {
        System.out.println(">>> AppRunner constructor");
        this.gitHubLookupService = gitHubLookupService;
    }

/*    @Override
    public void run(String... args) {
        System.out.println(">>> AppRunner.run() STARTED");

        // optional: try/catch everything inside
        try {
            gitHubLookupService.findUser("LadyMalande"); // whatever method you're calling
        } catch (Exception e) {
            System.out.println(">>> AppRunner.run() ERROR:");
            e.printStackTrace();
        }

        System.out.println(">>> AppRunner.run() FINISHED");
    }*/
//}
   @Override
    public void run(String... args) throws Exception {
       logger.info("Logger working!!!");
        try{
        // Start the clock
        long start = System.currentTimeMillis();

        // Kick of multiple, asynchronous lookups
        CompletableFuture<User> page1 = gitHubLookupService.findUser("PivotalSoftware");
        CompletableFuture<User> page2 = gitHubLookupService.findUser("CloudFoundry");
        CompletableFuture<User> page3 = gitHubLookupService.findUser("Spring-Projects");

        // Wait until they are all done
        CompletableFuture.allOf(page1,page2,page3).join();

        // Print results, including elapsed time
            System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
            System.out.println("--> " + page1.get());
            System.out.println("--> " + page2.get());
            System.out.println("--> " + page3.get());
    } catch (Exception e) {
            System.out.println("Error running AppRunner" + e);
    }

    }
/*    @Override
    public void run(String... args) throws Exception {
        // Start the clock
        long start = System.currentTimeMillis();

        // Kick of multiple, asynchronous lookups
        Map<String, String> config1 = rdFtoCSVWService.prepareConfigParameter(String.valueOf(TableChoice.ONE).toLowerCase(), String.valueOf(ParsingChoice.STREAMING).toLowerCase(), true).get();
        Map<String, String> config2 = rdFtoCSVWService.prepareConfigParameter(String.valueOf(TableChoice.ONE).toLowerCase(), String.valueOf(ParsingChoice.STREAMING).toLowerCase(), true).get();
        Map<String, String> config3 = rdFtoCSVWService.prepareConfigParameter(String.valueOf(TableChoice.ONE).toLowerCase(), String.valueOf(ParsingChoice.BIGFILESTREAMING).toLowerCase(), true).get();


        CompletableFuture<String> s1 = rdFtoCSVWService.getCSVString("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/test_scenarios_data/dissesto_2k_triples.nt", config1);
        CompletableFuture<String> s2 = rdFtoCSVWService.getCSVString("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/performance_tests_RDF_data/typy-d%C5%99evin.nt", config2);
        //CompletableFuture<String> s3 = rdFtoCSVWService.getCSVString("https://w3c.github.io/csvw/tests/test005.ttl", config3);
        //CompletableFuture<byte[]> page1 = rdFtoCSVWService.getCSVW(null, "https://w3c.github.io/csvw/tests/test005.ttl", String.valueOf(ParsingChoice.RDF4J), String.valueOf(TableChoice.ONE), true);
        //CompletableFuture<byte[]> page2 = rdFtoCSVWService.getCSVW(null, "https://w3c.github.io/csvw/tests/test005.ttl", String.valueOf(ParsingChoice.RDF4J), String.valueOf(TableChoice.ONE), true);
        //CompletableFuture<byte[]> page3 = rdFtoCSVWService.getCSVW(null, "https://w3c.github.io/csvw/tests/test005.ttl", String.valueOf(ParsingChoice.RDF4J), String.valueOf(TableChoice.ONE), true);
*//*        CompletableFuture<String> s4 = rdFtoCSVWService
                .prepareConfigParameter(
                        String.valueOf(TableChoice.ONE).toLowerCase(),
                        String.valueOf(ParsingChoice.BIGFILESTREAMING).toLowerCase(),
                        true
                ).thenCompose(config -> {
                    if (config == null) {
                        System.out.println("config is NULL");
                        return CompletableFuture.failedFuture(new IllegalStateException("Config is null"));
                    }
                    try {
                        // Defensive copy (if needed)
                        Map<String, String> safeConfig = new HashMap<>(config);
                        return rdFtoCSVWService.getCSVString("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/test_scenarios_data/dissesto_2k_triples.nt", safeConfig);
                    } catch (IOException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });*//*

     *//*    CompletableFuture<String> s5 = rdFtoCSVWService
                .prepareConfigParameter(
                        String.valueOf(TableChoice.ONE).toLowerCase(),
                        String.valueOf(ParsingChoice.RDF4J).toLowerCase(),
                        true
                ).thenCompose(config -> {
                    if (config == null) {
                        System.out.println("config is NULL");
                        return CompletableFuture.failedFuture(new IllegalStateException("Config is null"));
                    }
                    try {
                        // Defensive copy (if needed)
                        Map<String, String> safeConfig = new HashMap<>(config);
                        return rdFtoCSVWService.getCSVString("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/refs/heads/main/test_scenarios_data/dissesto_2k_triples.nt", safeConfig);
                    } catch (IOException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });

         CompletableFuture<String> s6 = null;
              rdFtoCSVWService
                .prepareConfigParameter(String.valueOf(TableChoice.ONE).toLowerCase(), String.valueOf(ParsingChoice.RDF4J).toLowerCase(), true).thenCompose(config ->
                        {
                            try {
                                return rdFtoCSVWService.getCSVString("https://w3c.github.io/csvw/tests/test005.ttl", config);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                );*//*

        CompletableFuture<Map<String, String>> configFuture1 = rdFtoCSVWService.prepareConfigParameter(String.valueOf(TableChoice.ONE).toLowerCase(),
                String.valueOf(ParsingChoice.RDF4J).toLowerCase(),
                true);
        CompletableFuture<Map<String, String>> configFuture2 = rdFtoCSVWService.prepareConfigParameter(String.valueOf(TableChoice.ONE).toLowerCase(),
                String.valueOf(ParsingChoice.RDF4J).toLowerCase(),
                true);

        //configFuture1.thenAccept(config -> System.out.println("Config1 hash: " + System.identityHashCode(config)));
        //configFuture2.thenAccept(config -> System.out.println("Config2 hash: " + System.identityHashCode(config)));
        // Wait until they are all done
        CompletableFuture.allOf(s1,s2).join();
        //CompletableFuture.allOf(s4).join();

        // Print results, including elapsed time
        logger.info("Elapsed time: " + (System.currentTimeMillis() - start));
        logger.info("--> " + s1.get());
        logger.info("--> " + s2.get());
        //logger.info("--> " + s3.get());
        //logger.info("--> " + s4.get());
        //logger.info("--> " + s5.get());
        //logger.info("--> " + s6.get());

    }*/

}
