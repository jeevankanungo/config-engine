package com.example.configclient.util;

import com.example.configclient.model.Configuration;
import com.example.configclient.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static ConfigService configService;

    public static void setConfigService(ConfigService service) {
        configService = service;
    }

    public static String getProperty(String applicationName, String profile, String propertyKey) {
        return getProperty(applicationName, profile, propertyKey, null);
    }

    public static String getProperty(String applicationName, String profile, String propertyKey, String defaultValue) {
        if (configService == null) {
            logger.warn("ConfigService not initialized, returning default value for {}", propertyKey);
            return defaultValue;
        }

        String configKey = applicationName + "-" + profile;
        Configuration config = configService.getConfigFromMemory(configKey);
        
        if (config == null || config.getProperties() == null) {
            logger.debug("Configuration not found for {}, returning default value for {}", configKey, propertyKey);
            return defaultValue;
        }

        Object value = config.getProperties().get(propertyKey);
        return value != null ? value.toString() : defaultValue;
    }

    public static void printAllConfigurations() {
        if (configService == null) {
            logger.warn("ConfigService not initialized");
            return;
        }

        Map<String, Configuration> allConfigs = configService.getAllConfigurations();
        
        if (allConfigs.isEmpty()) {
            System.out.println("No configurations loaded");
            return;
        }

        System.out.println("\n=== Loaded Configurations ===");
        allConfigs.forEach((key, config) -> {
            System.out.println("Key: " + key);
            System.out.println("Version: " + config.getVersion());
            System.out.println("Environment: " + config.getEnvironment());
            System.out.println("Last Updated: " + config.getLastUpdated());
            System.out.println("Properties: " + config.getProperties());
            System.out.println("---");
        });
    }
}
