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
        // from("quartz://observationTimer?cron=0 * * * * ?")
        from("quartz://observationTimer?cron=0/10+*+*+*+*+?")
            .setBody(simple("${random(0, 18) / 10 + 36.5}")) // Random value between 36.5 and 38.2
            // .setBody(simple("${random(100,200)}"))
            .bean(HL7Converter.class, "convertToHL7")
            .to("file:output?fileName=observation-${date:now:yyyyMMddHHmmss}.hl7");
    }
}