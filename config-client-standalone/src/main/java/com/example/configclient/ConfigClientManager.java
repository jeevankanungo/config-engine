package com.example.configclient;

import com.example.configclient.service.ConfigService;
import com.example.configclient.scheduler.ConfigScheduler;
import com.example.configclient.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Singleton manager for the config client that auto-starts when the class is loaded.
 * This allows Spring Boot applications to automatically initialize the config client
 * just by having this library on the classpath.
 */
public class ConfigClientManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigClientManager.class);
    
    private static volatile ConfigClientManager instance;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private final ScheduledExecutorService scheduler;
    private final ConfigService configService;
    private final ConfigScheduler configScheduler;
    
    // Static initializer - runs when class is first loaded
    static {
        try {
            // Auto-start when class is loaded (e.g., when Spring Boot app starts)
            getInstance().startIfNotRunning();
            logger.info("ConfigClientManager auto-initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to auto-initialize ConfigClientManager", e);
        }
    }
    
    private ConfigClientManager() {
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "config-client-scheduler");
            t.setDaemon(true); // Don't prevent JVM shutdown
            return t;
        });
        this.configService = new ConfigService();
        this.configScheduler = new ConfigScheduler(configService, scheduler);
        
        // Initialize ConfigUtil with the service
        ConfigUtil.setConfigService(configService);
        
        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "config-client-shutdown"));
    }
    
    /**
     * Get the singleton instance of ConfigClientManager
     */
    public static ConfigClientManager getInstance() {
        if (instance == null) {
            synchronized (ConfigClientManager.class) {
                if (instance == null) {
                    instance = new ConfigClientManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Start the config client if it's not already running
     */
    public synchronized void startIfNotRunning() {
        if (initialized.compareAndSet(false, true)) {
            try {
                start();
                logger.info("ConfigClientManager started successfully");
            } catch (Exception e) {
                initialized.set(false); // Reset on failure
                logger.error("Failed to start ConfigClientManager", e);
                throw new RuntimeException("Failed to start ConfigClientManager", e);
            }
        } else {
            logger.debug("ConfigClientManager is already running");
        }
    }
    
    /**
     * Force start the config client (restarts if already running)
     */
    public synchronized void forceStart() {
        if (initialized.get()) {
            stop();
        }
        initialized.set(false);
        startIfNotRunning();
    }
    
    /**
     * Check if the config client is running
     */
    public boolean isRunning() {
        return initialized.get();
    }
    
    /**
     * Get the config service for direct access
     */
    public ConfigService getConfigService() {
        return configService;
    }
    
    /**
     * Get the scheduler for direct access
     */
    public ConfigScheduler getConfigScheduler() {
        return configScheduler;
    }
    
    private void start() {
        logger.info("Starting configuration scheduler...");
        configScheduler.start();
        
        // Print initial status after a short delay
        scheduler.schedule(() -> {
            logger.info("=== Config Client Status ===");
            ConfigUtil.printAllConfigurations();
        }, 10, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    /**
     * Stop the config client
     */
    public synchronized void stop() {
        if (initialized.compareAndSet(true, false)) {
            logger.info("Stopping ConfigClientManager...");
            
            if (configScheduler != null) {
                configScheduler.stop();
            }
            
            logger.info("ConfigClientManager stopped");
        }
    }
    
    private void shutdown() {
        logger.info("Shutting down ConfigClientManager...");
        
        stop();
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("ConfigClientManager shutdown completed");
    }
}
