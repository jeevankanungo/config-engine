package com.example.configclient.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application properties", e);
        }
    }

    public static String getConfigServerUrl() {
        return properties.getProperty("config.server.url", "http://localhost:8888");
    }

    public static int getConfigServerTimeout() {
        return Integer.parseInt(properties.getProperty("config.server.timeout", "5000"));
    }

    public static List<String> getApplicationNames() {
        String apps = properties.getProperty("config.applications", "myapp");
        return Arrays.asList(apps.split(","));
    }

    public static List<String> getProfiles() {
        String profiles = properties.getProperty("config.profiles", "default");
        return Arrays.asList(profiles.split(","));
    }

    public static long getRefreshInterval() {
        return Long.parseLong(properties.getProperty("config.refresh.interval", "300000"));
    }

    public static long getHealthCheckInterval() {
        return Long.parseLong(properties.getProperty("config.health.check.interval", "600000"));
    }
    
    public static boolean isAutoStartEnabled() {
        return Boolean.parseBoolean(properties.getProperty("config.client.autostart", "true"));
    }
}
