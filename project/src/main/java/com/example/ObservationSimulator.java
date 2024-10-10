package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

import com.example.config.AppConfig;
import com.example.server.JettyServer;
import com.example.routes.ObservationRoutes;
import com.example.routes.HL7V2Routes;
import com.example.routes.FHIRRoutes;

public class ObservationSimulator {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        
        // Load configuration
        AppConfig config = new AppConfig();
        
        // Set up server
        JettyServer server = new JettyServer(config.getAppPort());
        server.start();

        // Add routes
        main.configure().addRoutesBuilder(new ObservationRoutes(config));
        main.configure().addRoutesBuilder(new HL7V2Routes());
        main.configure().addRoutesBuilder(new FHIRRoutes());

        // Run Camel
        main.run(args);
    }
}