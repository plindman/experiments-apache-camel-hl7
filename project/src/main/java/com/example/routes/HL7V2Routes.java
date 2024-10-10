package com.example;

import org.apache.camel.builder.RouteBuilder;

public class HL7V2Routes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:hl7v2")
            .bean(HL7V2Converter.class, "convertToHL7")
            .to("file:output?fileName=observation-hl7v2-${date:now:yyyyMMddHHmmss}.hl7");
    }
}