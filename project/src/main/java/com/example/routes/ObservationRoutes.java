package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;

import com.example.config.AppConfig;

public class ObservationRoutes extends RouteBuilder {
    private static final String SCHEDULER_ROUTE_ID = "schedulerRoute";
    private static final double MIN_TEMP = 35.0;
    private static final double MAX_TEMP = 42.0;

    private final AppConfig config;

    public ObservationRoutes(AppConfig config) {
        this.config = config;
    }

    @Override
    public void configure() throws Exception {
        onException(IllegalArgumentException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
            .setBody(exceptionMessage());

        from("quartz://observationTimer?cron=0/10+*+*+*+*+?").routeId(SCHEDULER_ROUTE_ID)
            .autoStartup(config.isSchedulerEnabled())
            .process(this::generateRandomObservation)
            .to("direct:processObservation");

        from("servlet:///trigger/hl7v2")
            .choice()
                .when(header("Content-Type").isEqualTo("application/x-www-form-urlencoded"))
                    .to("direct:parseFormData")
                .otherwise()
                    .process(this::generateRandomObservation)
            .end()
            .to("direct:hl7v2");

        from("servlet:///trigger/fhir")
            .choice()
                .when(header("Content-Type").isEqualTo("application/x-www-form-urlencoded"))
                    .to("direct:parseFormData")
                .otherwise()
                    .process(this::generateRandomObservation)
            .end()
            .to("direct:fhir");

        from("direct:parseFormData")
            .process(exchange -> {
                String bodyTemp = exchange.getIn().getHeader("bodyTemp", String.class);
                if (bodyTemp != null && !bodyTemp.isEmpty()) {
                    double temp = Double.parseDouble(bodyTemp);
                    validateTemperature(temp);
                    exchange.getMessage().setBody(String.format("%.1f", temp));
                } else {
                    generateRandomObservation(exchange);
                }
            });

        from("servlet:///toggle-scheduler")
            .process(exchange -> {
                // Toggle scheduler logic here
            });

        from("direct:processObservation")
            .to("log:body?showAll=true")
            .multicast()
                .to("direct:hl7v2", "direct:fhir");
    }

    private void generateRandomObservation(Exchange exchange) {
        double randomValue = MIN_TEMP + (Math.random() * (MAX_TEMP - MIN_TEMP));
        String formattedValue = String.format("%.1f", randomValue);
        exchange.getMessage().setBody(formattedValue);
    }

    private void validateTemperature(double temp) {
        if (temp < MIN_TEMP || temp > MAX_TEMP) {
            throw new IllegalArgumentException(
                String.format("Body temperature must be between %.1f°C and %.1f°C\n", MIN_TEMP, MAX_TEMP)
            );
        }
    }
}