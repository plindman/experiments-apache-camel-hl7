package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ObservationSimulator extends RouteBuilder {

    private static final String SCHEDULER_ROUTE_ID = "schedulerRoute";
    private static boolean isSchedulerEnabled = false;
    private static int appPort;

    // Define temperature range constants
    private static final double MIN_TEMP = 35.0; // Hypothermia threshold
    private static final double MAX_TEMP = 42.0; // Hyperpyrexia threshold

    public static void main(String[] args) throws Exception {

        appPort = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
        isSchedulerEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("SCHEDULER_ENABLED", "true"));

        Main main = new Main();
        main.configure().addRoutesBuilder(new ObservationSimulator());

        // Set up HTTP server
        Server server = new Server(appPort);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Set up Camel servlet
        ServletHolder servletHolder = new ServletHolder(new CamelHttpTransportServlet());
        servletHolder.setName("CamelServlet");
        context.addServlet(servletHolder, "/api/*");

        // Start the server
        server.start();

        // Run Camel
        main.run(args);
     }

    @Override
    public void configure() throws Exception {
        // Error handler
        onException(IllegalArgumentException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
            .setBody(exceptionMessage());

        // Scheduler route (unchanged)
        from("quartz://observationTimer?cron=0/10+*+*+*+*+?").routeId(SCHEDULER_ROUTE_ID)
            .autoStartup(isSchedulerEnabled)
            .process(this::generateRandomObservation)
            .to("direct:processObservation");

        // Updated HTTP triggers
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

        // Parse and validate form data
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

        // Toggle scheduler (unchanged)
        from("servlet:///toggle-scheduler")
            .process(exchange -> {
                // ... (unchanged)
            });

        // Common processing route (unchanged)
        from("direct:processObservation")
            .to("log:body?showAll=true")
            .multicast()
                .to("direct:hl7v2", "direct:fhir");

        // HL7v2 conversion route (unchanged)
        from("direct:hl7v2")
            .bean(HL7V2Converter.class, "convertToHL7")
            .to("file:output?fileName=observation-hl7v2-${date:now:yyyyMMddHHmmss}.hl7");

        // FHIR conversion route (unchanged)
        from("direct:fhir")
            .bean(HL7FHIRConverter.class, "convertToFHIR")
            .to("file:output?fileName=observation-fhir-${date:now:yyyyMMddHHmmss}.json");
    }

    private void generateRandomObservation(org.apache.camel.Exchange exchange) {
        double randomValue = MIN_TEMP + (Math.random() * (MAX_TEMP - MIN_TEMP));
        String formattedValue = String.format("%.1f", randomValue);
        exchange.getMessage().setBody(formattedValue);
    }

    private void validateTemperature(double temp) {
        if (temp < MIN_TEMP || temp > MAX_TEMP) {
            throw new IllegalArgumentException(
                String.format("Body temperature must be between %.1f°C and %.1f°C", MIN_TEMP, MAX_TEMP)
            );
        }
    }
}