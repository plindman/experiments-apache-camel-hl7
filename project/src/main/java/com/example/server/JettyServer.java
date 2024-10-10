package com.example;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class JettyServer {
    private final Server server;

    public JettyServer(int port) {
        this.server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder servletHolder = new ServletHolder(new CamelHttpTransportServlet());
        servletHolder.setName("CamelServlet");
        context.addServlet(servletHolder, "/api/*");
    }

    public void start() throws Exception {
        server.start();
    }
}