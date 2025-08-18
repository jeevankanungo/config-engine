package com.example.configclient.scheduler;

import com.example.configclient.config.AppConfig;
import com.example.configclient.model.Configuration;
import com.example.configclient.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfigScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ConfigScheduler.class);
    
    private final ConfigService configService;
    private final ScheduledExecutorService scheduler;
    private final List<String> applicationNames;
    private final List<String> profiles;

    public ConfigScheduler(ConfigService configService, ScheduledExecutorService scheduler) {
        this.configService = configService;
        this.scheduler = scheduler;
        this.applicationNames = AppConfig.getApplicationNames();
        this.profiles = AppConfig.getProfiles();
    }

    public void start() {
        // Initial configuration load
        logger.info("Performing initial configuration load...");
        refreshConfigurations();
        
        // Schedule periodic refresh
        long refreshInterval = AppConfig.getRefreshInterval();
        scheduler.scheduleAtFixedRate(
            this::refreshConfigurations,
            refreshInterval,
            refreshInterval,
            TimeUnit.MILLISECONDS
        );
        
        // Schedule health checks
        long healthCheckInterval = AppConfig.getHealthCheckInterval();
        scheduler.scheduleAtFixedRate(
            this::performHealthCheck,
            healthCheckInterval,
            healthCheckInterval,
            TimeUnit.MILLISECONDS
        );
        
        logger.info("Configuration scheduler started - refresh interval: {}ms, health check interval: {}ms",
                   refreshInterval, healthCheckInterval);
    }

    public void stop() {
        logger.info("Stopping configuration scheduler...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Configuration scheduler stopped");
    }

    private void refreshConfigurations() {
        logger.debug("Starting scheduled configuration refresh...");
        
        int successCount = 0;
        int totalCount = 0;
        
        for (String appName : applicationNames) {
            for (String profile : profiles) {
                totalCount++;
                try {
                    String cacheKey = appName + "-" + profile;
                    Configuration newConfig = configService.fetchConfigurationFromServer(appName, profile);
                    
                    if (newConfig != null) {
                        Configuration existingConfig = configService.getConfigFromMemory(cacheKey);
                        
                        if (existingConfig == null || isConfigurationChanged(existingConfig, newConfig)) {
                            configService.updateConfigInMemory(cacheKey, newConfig);
                            logger.info("Configuration refreshed for {}-{}", appName, profile);
                        } else {
                            logger.debug("No changes detected for {}-{}", appName, profile);
                        }
                        successCount++;
                    } else {
                        logger.warn("Failed to fetch configuration for {}-{}", appName, profile);
                    }
                } catch (Exception e) {
                    logger.error("Error refreshing configuration for {}-{}: {}", 
                               appName, profile, e.getMessage(), e);
                }
            }
        }
        
        logger.info("Configuration refresh completed - Success: {}/{}", successCount, totalCount);
    }

    private void performHealthCheck() {
        logger.debug("Performing configuration health check...");
        
        int totalConfigs = configService.getCacheSize();
        int expectedConfigs = applicationNames.size() * profiles.size();
        
        if (totalConfigs < expectedConfigs) {
            logger.warn("Configuration health check: Missing configurations. Expected: {}, Found: {}", 
                       expectedConfigs, totalConfigs);
        } else {
            logger.debug("Configuration health check passed. Total configurations: {}", totalConfigs);
        }
    }

    private boolean isConfigurationChanged(Configuration existing, Configuration newConfig) {
        if (existing == null && newConfig == null) return false;
        if (existing == null || newConfig == null) return true;
        
        // Compare versions if available
        if (existing.getVersion() != null && newConfig.getVersion() != null) {
            return !existing.getVersion().equals(newConfig.getVersion());
        }
        
        // Fallback to comparing properties
        if (existing.getProperties() != null && newConfig.getProperties() != null) {
            return !existing.getProperties().equals(newConfig.getProperties());
        }
        
        return !existing.equals(newConfig);
    }
}
