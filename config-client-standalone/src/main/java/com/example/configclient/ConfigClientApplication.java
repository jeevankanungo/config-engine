package com.example.configclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for standalone execution.
 * When used as a library, ConfigClientManager will auto-start via static initialization.
 */
public class ConfigClientApplication {
    private static final Logger logger = LoggerFactory.getLogger(ConfigClientApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Config Client Application in standalone mode...");
        
        try {
            // Get the manager instance (this will auto-start if not already running)
            ConfigClientManager manager = ConfigClientManager.getInstance();
            
            // Ensure it's started
            manager.startIfNotRunning();
            
            logger.info("Config Client Application started successfully");
            logger.info("Press Ctrl+C to stop the application");
            
            // Keep the application running
            Thread.currentThread().join();
            
        } catch (InterruptedException e) {
            logger.warn("Application interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Application failed to start", e);
            System.exit(1);
        }
    }
}
