package org.rdftocsvconverter.RDFtoCSVW.api.controller;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
public class BriefingController {

    // Create a Sink to hold the briefing stream
    private final Sinks.Many<String> briefingSink = Sinks.many().multicast().onBackpressureBuffer();

    @GetMapping(value = "/briefing", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendBriefing() {
        // Stream updates from the sink
        return briefingSink.asFlux().delayElements(Duration.ofSeconds(1));
        //return Flux.interval(Duration.ofSeconds(1)).map(seq -> "Briefing update " + seq);
    }

    // This method triggers a briefing message from another request
    @GetMapping("/triggerBriefing")
    public String triggerBriefing() {
        // Publish a new update to all subscribers
        briefingSink.tryEmitNext("Manual briefing update at " + System.currentTimeMillis());
        return "Briefing update sent!";
    }
}