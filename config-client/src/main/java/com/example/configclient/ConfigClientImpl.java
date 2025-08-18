package com.example.configclient;

import com.example.configclient.cache.ConfigCache;
import com.example.configclient.exception.ConfigClientException;
import com.example.configclient.http.HttpClient;
import com.example.configclient.listener.ConfigChangeEvent;
import com.example.configclient.listener.ConfigChangeListener;
import com.example.configclient.model.Configuration;
import com.example.configclient.scheduler.ConfigRefreshScheduler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class ConfigClientImpl implements ConfigClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigClientImpl.class);
    
    private final String configServerUrl;
    private final String applicationName;
    private final String profile;
    private final HttpClient httpClient;
    private final ConfigCache cache;
    private final ObjectMapper objectMapper;
    private final int maxRetries;
    private final long retryDelayMs;
    private final ExecutorService executorService;
    
    private final AtomicBoolean healthy = new AtomicBoolean(true);
    private final AtomicLong lastRefreshTime = new AtomicLong(0);
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    // Statistics
    private final LongAdder totalRefreshes = new LongAdder();
    private final LongAdder successfulRefreshes = new LongAdder();
    private final LongAdder failedRefreshes = new LongAdder();
    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder cacheMisses = new LongAdder();
    private final List<Long> refreshTimes = new CopyOnWriteArrayList<>();
    
    private ConfigRefreshScheduler scheduler;
    private volatile Configuration currentConfig;
    
    public ConfigClientImpl(String configServerUrl, 
                           String applicationName, 
                           String profile,
                           HttpClient httpClient,
                           ConfigCache cache,
                           int maxRetries,
                           long retryDelayMs) {
        this.configServerUrl = configServerUrl;
        this.applicationName = applicationName;
        this.profile = profile;
        this.httpClient = httpClient;
        this.cache = cache;
        this.objectMapper = new ObjectMapper();
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.executorService = ForkJoinPool.commonPool();
        
        // Initial fetch
        try {
            this.currentConfig = fetchConfiguration();
        } catch (Exception e) {
            logger.error("Failed to fetch initial configuration", e);
            this.currentConfig = new Configuration(applicationName, profile);
        }
    }
    
    @Override
    public Configuration fetchConfiguration() {
        var url = STR."\{configServerUrl}/\{applicationName}/\{profile}/complete";
        logger.info("Fetching configuration from: {}", url);
        
        var startTime = Instant.now();
        totalRefreshes.increment();
        
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                var response = httpClient.get(url);
                var newConfig = parseConfiguration(response);
                
                // Update cache
                cache.put(STR."\{applicationName}-\{profile}", newConfig);
                
                // Check for changes
                if (currentConfig != null && hasChanges(currentConfig, newConfig)) {
                    notifyListeners(currentConfig, newConfig);
                }
                
                currentConfig = newConfig;
                lastRefreshTime.set(System.currentTimeMillis());
                healthy.set(true);
                successfulRefreshes.increment();
                
                var duration = Duration.between(startTime, Instant.now()).toMillis();
                refreshTimes.add(duration);
                
                logger.info("Successfully fetched configuration in {}ms", duration);
                return newConfig;
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Attempt {} failed: {}", i + 1, e.getMessage());
                
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelayMs * (i + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        healthy.set(false);
        failedRefreshes.increment();
        throw new ConfigClientException(
            STR."Failed to fetch configuration after \{maxRetries} attempts", 
            lastException
        );
    }
    
    @Override
    public CompletableFuture<Configuration> fetchConfigurationAsync() {
        return CompletableFuture.supplyAsync(this::fetchConfiguration, executorService);
    }
    
    @Override
    public Configuration getConfiguration() {
        if (currentConfig == null) {
            cacheMisses.increment();
            currentConfig = cache.get(STR."\{applicationName}-\{profile}")
                .orElseGet(() -> fetchConfiguration());
        } else {
            cacheHits.increment();
        }
        return currentConfig;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfigValue(String key, Class<T> type) {
        var config = getConfiguration();
        var value = config.getConfig().get(key);
        
        if (value == null) {
            return Optional.empty();
        }
        
        // Use pattern matching (Java 17)
        return switch (type.getSimpleName()) {
            case "String" -> Optional.of((T) String.valueOf(value));
            case "Integer" -> Optional.of((T) Integer.valueOf(value.toString()));
            case "Long" -> Optional.of((T) Long.valueOf(value.toString()));
            case "Double" -> Optional.of((T) Double.valueOf(value.toString()));
            case "Boolean" -> Optional.of((T) Boolean.valueOf(value.toString()));
            default -> {
                try {
                    yield Optional.of(objectMapper.convertValue(value, type));
                } catch (Exception e) {
                    logger.warn("Failed to convert {} to {}", value, type, e);
                    yield Optional.empty();
                }
            }
        };
    }
    
    @Override
    public boolean isFeatureEnabled(String featureName) {
        return getConfiguration().getFeatures().getOrDefault(featureName, false);
    }
    
    @Override
    public Optional<String> getSecret(String secretKey) {
        return Optional.ofNullable(getConfiguration().getSecrets().get(secretKey));
    }
    
    @Override
    public Map<String, Object> getAllConfig() {
        return Map.copyOf(getConfiguration().getConfig());
    }
    
    @Override
    public Map<String, Boolean> getAllFeatures() {
        return Map.copyOf(getConfiguration().getFeatures());
    }
    
    @Override
    public Map<String, String> getAllSecrets() {
        return Map.copyOf(getConfiguration().getSecrets());
    }
    
    @Override
    public void refresh() {
        fetchConfiguration();
    }
    
    @Override
    public CompletableFuture<Void> refreshAsync() {
        return CompletableFuture.runAsync(this::refresh, executorService);
    }
    
    @Override
    public void startAutoRefresh(long intervalMinutes) {
        if (scheduler != null) {
            scheduler.stop();
        }
        
        scheduler = new ConfigRefreshScheduler(this::refresh, intervalMinutes);
        scheduler.start();
        logger.info("Started auto-refresh with interval: {} minutes", intervalMinutes);
    }
    
    @Override
    public void stopAutoRefresh() {
        if (scheduler != null) {
            scheduler.stop();
            scheduler = null;
            logger.info("Stopped auto-refresh");
        }
    }
    
    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public boolean isHealthy() {
        return healthy.get();
    }
    
    @Override
    public long getLastRefreshTime() {
        return lastRefreshTime.get();
    }
    
    @Override
    public ClientStatistics getStatistics() {
        var avgRefreshTime = refreshTimes.isEmpty() ? 0.0 : 
            refreshTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        return new ClientStatistics(
            totalRefreshes.sum(),
            successfulRefreshes.sum(),
            failedRefreshes.sum(),
            cacheHits.sum(),
            cacheMisses.sum(),
            avgRefreshTime
        );
    }
    
    @Override
    public void close() throws IOException {
        stopAutoRefresh();
        httpClient.close();
        cache.clear();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private Configuration parseConfiguration(String json) throws Exception {
        var response = objectMapper.readValue(json, 
            new TypeReference<Map<String, Object>>() {});
        
        var config = new Configuration(applicationName, profile);
        
        // Parse using pattern matching
        response.forEach((key, value) -> {
            switch (key) {
                case "config" -> {
                    if (value instanceof Map<?, ?> map) {
                        config.setConfig(convertMap(map));
                    }
                }
                case "features" -> {
                    if (value instanceof Map<?, ?> map) {
                        config.setFeatures(convertToFeatures(map));
                    }
                }
                case "secrets" -> {
                    if (value instanceof Map<?, ?> map) {
                        config.setSecrets(convertToSecrets(map));
                    }
                }
                case "metadata" -> {
                    if (value instanceof Map<?, ?> map) {
                        config.setMetadata(convertMap(map));
                    }
                }
                default -> logger.debug("Unknown configuration key: {}", key);
            }
        });
        
        return config;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertMap(Map<?, ?> map) {
        var result = new HashMap<String, Object>();
        map.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }
    
    private Map<String, Boolean> convertToFeatures(Map<?, ?> map) {
        var result = new HashMap<String, Boolean>();
        map.forEach((k, v) -> result.put(String.valueOf(k), Boolean.valueOf(v.toString())));
        return result;
    }
    
    private Map<String, String> convertToSecrets(Map<?, ?> map) {
        var result = new HashMap<String, String>();
        map.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
        return result;
    }
    
    private boolean hasChanges(Configuration oldConfig, Configuration newConfig) {
        return !oldConfig.getConfig().equals(newConfig.getConfig()) ||
               !oldConfig.getFeatures().equals(newConfig.getFeatures()) ||
               !oldConfig.getSecrets().equals(newConfig.getSecrets());
    }
    
    private void notifyListeners(Configuration oldConfig, Configuration newConfig) {
        var event = new ConfigChangeEvent(oldConfig, newConfig);
        listeners.parallelStream().forEach(listener -> {
            try {
                listener.onConfigChange(event);
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        });
    }
}
