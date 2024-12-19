package org.rdftocsvconverter.RDFtoCSVW.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Table setting for output - one or more created tables.")
public enum TableChoice {
    ONE, MORE
}
