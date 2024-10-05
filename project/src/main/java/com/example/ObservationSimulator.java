package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.CamelContext;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ObservationSimulator extends RouteBuilder {

    private static final String SCHEDULER_ROUTE_ID = "schedulerRoute";
    private static boolean isSchedulerEnabled = false;
    private static int appPort;

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
        // Scheduler route
        from("quartz://observationTimer?cron=0/10+*+*+*+*+?").routeId(SCHEDULER_ROUTE_ID)
            .autoStartup(isSchedulerEnabled)
            .process(this::generateRandomObservation)
            .to("direct:processObservation");

        // HTTP triggers
        from("servlet:///trigger/hl7v2")
            .process(this::generateRandomObservation)
            .to("direct:hl7v2");

        from("servlet:///trigger/fhir")
            .process(this::generateRandomObservation)
            .to("direct:fhir");

        // Toggle scheduler
        from("servlet:///toggle-scheduler")
            .process(exchange -> {
                isSchedulerEnabled = !isSchedulerEnabled;
                CamelContext context = exchange.getContext();
                if (isSchedulerEnabled) {
                    context.getRouteController().startRoute(SCHEDULER_ROUTE_ID);
                } else {
                    context.getRouteController().stopRoute(SCHEDULER_ROUTE_ID);
                }
                exchange.getMessage().setBody("Scheduler is now " + (isSchedulerEnabled ? "enabled" : "disabled\n"));
            });

        // Common processing route
        from("direct:processObservation")
            .to("log:body?showAll=true")
            .multicast()
                .to("direct:hl7v2", "direct:fhir");

        // HL7v2 conversion route
        from("direct:hl7v2")
            .bean(HL7V2Converter.class, "convertToHL7")
            .to("file:output?fileName=observation-hl7v2-${date:now:yyyyMMddHHmmss}.hl7");

        // FHIR conversion route
        from("direct:fhir")
            .bean(HL7FHIRConverter.class, "convertToFHIR")
            .to("file:output?fileName=observation-fhir-${date:now:yyyyMMddHHmmss}.json");
    }

    private void generateRandomObservation(org.apache.camel.Exchange exchange) {
        double min = 36.5;
        double max = 38.2;
        double randomValue = min + (Math.random() * (max - min));
        String formattedValue = String.format("%.1f", randomValue);
        exchange.getMessage().setBody(formattedValue);
    }
}