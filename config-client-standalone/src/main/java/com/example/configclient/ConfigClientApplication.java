package com.example.configclient;

import com.example.configclient.config.AppConfig;
import com.example.configclient.scheduler.ConfigScheduler;
import com.example.configclient.service.ConfigService;
import com.example.configclient.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ConfigClientApplication {
    private static final Logger logger = LoggerFactory.getLogger(ConfigClientApplication.class);
    
    private final ScheduledExecutorService scheduler;
    private final ConfigService configService;
    private final ConfigScheduler configScheduler;

    public ConfigClientApplication() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.configService = new ConfigService();
        this.configScheduler = new ConfigScheduler(configService, scheduler);
        
        // Initialize ConfigUtil with the service
        ConfigUtil.setConfigService(configService);
    }

    public static void main(String[] args) {
        logger.info("Starting Config Client Application...");
        
        ConfigClientApplication app = new ConfigClientApplication();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));
        
        try {
            app.start();
            
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

    private void start() {
        logger.info("Initializing configuration scheduler...");
        configScheduler.start();
        logger.info("Config Client Application started successfully");
        
        // Print initial status after a short delay
        scheduler.schedule(() -> {
            logger.info("=== Application Status ===");
            ConfigUtil.printAllConfigurations();
        }, 10, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void shutdown() {
        logger.info("Shutting down Config Client Application...");
        
        if (configScheduler != null) {
            configScheduler.stop();
        }
        
        if (scheduler != null) {
            scheduler.shutdown();
        }
        
        logger.info("Config Client Application shutdown completed");
    }
}
