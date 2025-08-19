package com.example.configclient.scheduler;

import com.example.configclient.config.AppConfig;
import com.example.configclient.model.Configuration;
import com.example.configclient.service.ConfigService;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConfigScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ConfigScheduler.class);
    
    private final ConfigService configService;
    private final ScheduledExecutorService scheduler;
    private final List<String> applicationNames;
    private final List<String> profiles;
    
    private ScheduledFuture<?> refreshTask;
    private ScheduledFuture<?> healthCheckTask;
    private volatile boolean running = false;

    public ConfigScheduler(ConfigService configService, ScheduledExecutorService scheduler) {
        this.configService = configService;
        this.scheduler = scheduler;
        this.applicationNames = AppConfig.getApplicationNames();
        this.profiles = AppConfig.getProfiles();
    }

    public synchronized void start() {
        if (running) {
            logger.debug("ConfigScheduler is already running");
            return;
        }
        
        // Initial configuration load
        logger.info("Performing initial configuration load...");
        refreshConfigurations();
        
        // Schedule periodic refresh
        long refreshInterval = AppConfig.getRefreshInterval();
        refreshTask = scheduler.scheduleAtFixedRate(
            this::refreshConfigurations,
            refreshInterval,
            refreshInterval,
            TimeUnit.MILLISECONDS
        );
        
        // Schedule health checks
        long healthCheckInterval = AppConfig.getHealthCheckInterval();
        healthCheckTask = scheduler.scheduleAtFixedRate(
            this::performHealthCheck,
            healthCheckInterval,
            healthCheckInterval,
            TimeUnit.MILLISECONDS
        );
        
        running = true;
        logger.info("Configuration scheduler started - refresh interval: {}ms, health check interval: {}ms",
                   refreshInterval, healthCheckInterval);
    }

    public synchronized void stop() {
        if (!running) {
            logger.debug("ConfigScheduler is not running");
            return;
        }
        
        logger.info("Stopping configuration scheduler...");
        
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
        
        if (healthCheckTask != null) {
            healthCheckTask.cancel(false);
        }
        
        running = false;
        logger.info("Configuration scheduler stopped");
    }
    
    public boolean isRunning() {
        return running;
    }

    private void refreshConfigurations() {
        logger.debug("Starting scheduled configuration refresh...");
        
        int successCount = 0;
        int totalCount = 0;
        int changedCount = 0;
        
        for (String appName : applicationNames) {
            for (String profile : profiles) {
                totalCount++;
                try {
                    String cacheKey = appName + "-" + profile;
                    Configuration newConfig = configService.fetchConfigurationFromServer(appName, profile);
                    
                    if (newConfig != null) {
                        Configuration existingConfig = configService.getConfigFromMemory(cacheKey);
                        
                        ConfigChangeResult changeResult = isConfigurationChanged(existingConfig, newConfig);
                        
                        if (changeResult.hasChanged()) {
                            configService.updateConfigInMemory(cacheKey, newConfig);
                            changedCount++;
                            
                            if (changeResult.hasDetails()) {
                                logger.info("Configuration refreshed for {}-{} with changes: {}", 
                                           appName, profile, changeResult.getChangeDescription());
                            } else {
                                logger.info("Configuration refreshed for {}-{}", appName, profile);
                            }
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
        
        logger.info("Configuration refresh completed - Success: {}/{}, Changed: {}", 
                   successCount, totalCount, changedCount);
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

    private ConfigChangeResult isConfigurationChanged(Configuration existing, Configuration newConfig) {
        if (existing == null && newConfig == null) {
            return new ConfigChangeResult(false, null);
        }
        if (existing == null || newConfig == null) {
            return new ConfigChangeResult(true, "Configuration added/removed");
        }
        
        // Compare versions if available
        if (existing.getVersion() != null && newConfig.getVersion() != null) {
            if (!existing.getVersion().equals(newConfig.getVersion())) {
                return new ConfigChangeResult(true, 
                    String.format("Version changed: %s -> %s", existing.getVersion(), newConfig.getVersion()));
            }
        }
        
        // Use Guava MapDifference for detailed property comparison
        if (existing.getProperties() != null && newConfig.getProperties() != null) {
            MapDifference<String, Object> diff = Maps.difference(existing.getProperties(), newConfig.getProperties());
            
            if (!diff.areEqual()) {
                StringBuilder changeDesc = new StringBuilder();
                
                if (!diff.entriesOnlyOnLeft().isEmpty()) {
                    changeDesc.append("Removed: ").append(diff.entriesOnlyOnLeft().keySet()).append("; ");
                }
                
                if (!diff.entriesOnlyOnRight().isEmpty()) {
                    changeDesc.append("Added: ").append(diff.entriesOnlyOnRight().keySet()).append("; ");
                }
                
                if (!diff.entriesDiffering().isEmpty()) {
                    changeDesc.append("Modified: ").append(diff.entriesDiffering().keySet()).append("; ");
                }
                
                return new ConfigChangeResult(true, changeDesc.toString().trim());
            }
        }
        
        // Fallback to basic equals comparison
        return new ConfigChangeResult(!existing.equals(newConfig), "Basic property change detected");
    }
    
    /**
     * Helper class to encapsulate configuration change results
     */
    private static class ConfigChangeResult {
        private final boolean changed;
        private final String changeDescription;
        
        public ConfigChangeResult(boolean changed, String changeDescription) {
            this.changed = changed;
            this.changeDescription = changeDescription;
        }
        
        public boolean hasChanged() {
            return changed;
        }
        
        public boolean hasDetails() {
            return changeDescription != null && !changeDescription.trim().isEmpty();
        }
        
        public String getChangeDescription() {
            return changeDescription;
        }
    }
}
