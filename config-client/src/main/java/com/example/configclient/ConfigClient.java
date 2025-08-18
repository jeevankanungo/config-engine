package com.example.configclient;

import com.example.configclient.listener.ConfigChangeListener;
import com.example.configclient.model.Configuration;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Main interface for Config Client using Java 17 features
 */
public sealed interface ConfigClient extends Closeable 
    permits ConfigClientImpl {
    
    /**
     * Fetch configuration synchronously
     */
    Configuration fetchConfiguration();
    
    /**
     * Fetch configuration asynchronously
     */
    CompletableFuture<Configuration> fetchConfigurationAsync();
    
    /**
     * Get current configuration from cache
     */
    Configuration getConfiguration();
    
    /**
     * Get a specific configuration value with type safety
     */
    <T> Optional<T> getConfigValue(String key, Class<T> type);
    
    /**
     * Get a configuration value as string
     */
    default Optional<String> getConfigValue(String key) {
        return getConfigValue(key, String.class);
    }
    
    /**
     * Check if a feature is enabled
     */
    boolean isFeatureEnabled(String featureName);
    
    /**
     * Get a secret value
     */
    Optional<String> getSecret(String secretKey);
    
    /**
     * Get all configuration as Map
     */
    Map<String, Object> getAllConfig();
    
    /**
     * Get all feature flags
     */
    Map<String, Boolean> getAllFeatures();
    
    /**
     * Get all secrets
     */
    Map<String, String> getAllSecrets();
    
    /**
     * Manually trigger refresh
     */
    void refresh();
    
    /**
     * Refresh asynchronously
     */
    CompletableFuture<Void> refreshAsync();
    
    /**
     * Start auto-refresh scheduler
     */
    void startAutoRefresh(long intervalMinutes);
    
    /**
     * Stop auto-refresh scheduler
     */
    void stopAutoRefresh();
    
    /**
     * Add configuration change listener
     */
    void addChangeListener(ConfigChangeListener listener);
    
    /**
     * Remove configuration change listener
     */
    void removeChangeListener(ConfigChangeListener listener);
    
    /**
     * Check if client is healthy
     */
    boolean isHealthy();
    
    /**
     * Get last refresh timestamp
     */
    long getLastRefreshTime();
    
    /**
     * Get client statistics
     */
    ClientStatistics getStatistics();
    
    /**
     * Client statistics record
     */
    record ClientStatistics(
        long totalRefreshes,
        long successfulRefreshes,
        long failedRefreshes,
        long cacheHits,
        long cacheMisses,
        double averageRefreshTime
    ) {}
}
