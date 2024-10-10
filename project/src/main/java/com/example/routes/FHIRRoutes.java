package com.example;

import org.apache.camel.builder.RouteBuilder;

public class FHIRRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:fhir")
            .bean(HL7FHIRConverter.class, "convertToFHIR")
            .to("file:output?fileName=observation-fhir-${date:now:yyyyMMddHHmmss}.json");
    }
}