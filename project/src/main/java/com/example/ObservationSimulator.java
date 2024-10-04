package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class ObservationSimulator extends RouteBuilder {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new ObservationSimulator());
        main.run(args);
    }

    @Override
    public void configure() throws Exception {
        from("quartz://observationTimer?cron=0/10+*+*+*+*+?")
            .process(exchange -> {
                double min = 36.5;
                double max = 38.2;
                double randomValue = min + (Math.random() * (max - min));
                String formattedValue = String.format("%.1f", randomValue);
                exchange.getIn().setBody(formattedValue);                
            })
            .to("log:body?showAll=true") // Log the body after setting it
            .multicast()
                .to("direct:hl7v2", "direct:fhir");

        from("direct:hl7v2")
            .bean(HL7V2Converter.class, "convertToHL7")
            .to("file:output?fileName=observation-hl7v2-${date:now:yyyyMMddHHmmss}.hl7");

        from("direct:fhir")
            .bean(HL7FHIRConverter.class, "convertToFHIR")
            .to("file:output?fileName=observation-fhir-${date:now:yyyyMMddHHmmss}.json");
    }
}