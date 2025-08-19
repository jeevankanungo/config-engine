package com.example.configclient.util;

import com.example.configclient.ConfigClientManager;
import com.example.configclient.model.Configuration;
import com.example.configclient.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Library interface for Spring Boot applications.
 * This class provides easy access to the config client functionality.
 */
public class ConfigClientLibrary {
    private static final Logger logger = LoggerFactory.getLogger(ConfigClientLibrary.class);
    
    /**
     * Get a configuration property value
     */
    public static String getProperty(String applicationName, String profile, String propertyKey) {
        return ConfigUtil.getProperty(applicationName, profile, propertyKey);
    }
    
    /**
     * Get a configuration property value with default
     */
    public static String getProperty(String applicationName, String profile, String propertyKey, String defaultValue) {
        return ConfigUtil.getProperty(applicationName, profile, propertyKey, defaultValue);
    }
    
    /**
     * Get the complete configuration for an application-profile combination
     */
    public static Configuration getConfiguration(String applicationName, String profile) {
        ConfigService configService = ConfigClientManager.getInstance().getConfigService();
        String cacheKey = applicationName + "-" + profile;
        return configService.getConfigFromMemory(cacheKey);
    }
    
    /**
     * Get all loaded configurations
     */
    public static Map<String, Configuration> getAllConfigurations() {
        ConfigService configService = ConfigClientManager.getInstance().getConfigService();
        return configService.getAllConfigurations();
    }
    
    /**
     * Force refresh configurations immediately
     */
    public static void refreshConfigurations() {
        try {
            ConfigClientManager manager = ConfigClientManager.getInstance();
            if (manager.isRunning()) {
                // Stop and restart to force immediate refresh
                manager.forceStart();
                logger.info("Configuration refresh forced");
            } else {
                logger.warn("Config client is not running, starting it now");
                manager.startIfNotRunning();
            }
        } catch (Exception e) {
            logger.error("Failed to refresh configurations", e);
        }
    }
    
    /**
     * Check if config client is running
     */
    public static boolean isRunning() {
        return ConfigClientManager.getInstance().isRunning();
    }
    
    /**
     * Get the number of loaded configurations
     */
    public static int getConfigurationCount() {
        ConfigService configService = ConfigClientManager.getInstance().getConfigService();
        return configService.getCacheSize();
    }
    
    /**
     * Print all configurations to console (useful for debugging)
     */
    public static void printConfigurations() {
        ConfigUtil.printAllConfigurations();
    }
}
