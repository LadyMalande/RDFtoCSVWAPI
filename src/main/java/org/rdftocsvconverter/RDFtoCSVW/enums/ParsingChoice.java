package org.rdftocsvconverter.RDFtoCSVW.enums;

/**
 * The class gives a list of available conversion methods.
 */
public enum ParsingChoice {
    /**
     * Data will be parsed using RDF4J methods.
     */
    RDF4J,
    /**
     * Streaming parsing choice - parses data triple after triple, simultaneously updates metadata nad CSVs.
     */
    STREAMING,
    /**
     * Bigfilestreaming parsing choice - in first walk creates metadata, in second walk writes data to CSV by created metadata.
     */
    BIGFILESTREAMING
}
