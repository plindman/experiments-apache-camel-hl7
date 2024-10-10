package com.example.routes;

import org.apache.camel.builder.RouteBuilder;

import com.example.converters.HL7FHIRConverter;

public class FHIRRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:fhir")
            .bean(HL7FHIRConverter.class, "convertToFHIR")
            .to("file:output?fileName=observation-fhir-${date:now:yyyyMMddHHmmss}.json");
    }
}