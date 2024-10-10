package com.example.server;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;

import java.net.URL;

public class JettyServer {
    private final Server server;

    public JettyServer(int port) {
        this.server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Set up Camel servlet for API endpoints
        ServletHolder camelServletHolder = new ServletHolder("CamelServlet", new CamelHttpTransportServlet());
        context.addServlet(camelServletHolder, "/api/*");

        // Set up DefaultServlet for serving static files
        ServletHolder defaultServletHolder = new ServletHolder("default", DefaultServlet.class);
        defaultServletHolder.setInitParameter("resourceBase", getClass().getResource("/static").toExternalForm());
        defaultServletHolder.setInitParameter("dirAllowed", "false");
        context.addServlet(defaultServletHolder, "/");

        // Set welcome file
        context.setWelcomeFiles(new String[]{"index.html"});
    }

    public void start() throws Exception {
        server.start();
    }
}