package org.rdftocsvconverter.RDFtoCSVW.enums;


/**
 * The class gives a choice of two parameter values for how to set parameters to convert the RDF data into ONE or MORE CSV files.
 */
public enum TableChoice {
    /**
     * Parameter for creating only one table during the conversion.
     */
    ONE,
    /**
     * Parameter for creating more tables if possible during the conversion.
     */
    MORE
}
