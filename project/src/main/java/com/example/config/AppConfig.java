package com.example;

public class AppConfig {
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_SCHEDULER_ENABLED = "true";

    private final int appPort;
    private final boolean schedulerEnabled;

    public AppConfig() {
        this.appPort = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", DEFAULT_PORT));
        this.schedulerEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("SCHEDULER_ENABLED", DEFAULT_SCHEDULER_ENABLED));
    }

    public int getAppPort() {
        return appPort;
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }
}