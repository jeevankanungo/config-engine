package com.example.configclient.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration model using Java 17 records for immutable data
 */
public class Configuration {
    
    private final String applicationName;
    private final String profile;
    private final Map<String, Object> config;
    private final Map<String, Boolean> features;
    private final Map<String, String> secrets;
    private final Map<String, Object> metadata;
    private final long timestamp;
    
    public Configuration(String applicationName, String profile) {
        this.applicationName = applicationName;
        this.profile = profile;
        this.config = new ConcurrentHashMap<>();
        this.features = new ConcurrentHashMap<>();
        this.secrets = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    // Copy constructor
    public Configuration(Configuration other) {
        this.applicationName = other.applicationName;
        this.profile = other.profile;
        this.config = new ConcurrentHashMap<>(other.config);
        this.features = new ConcurrentHashMap<>(other.features);
        this.secrets = new ConcurrentHashMap<>(other.secrets);
        this.metadata = new ConcurrentHashMap<>(other.metadata);
        this.timestamp = other.timestamp;
    }
    
    // Getters return immutable copies
    public String getApplicationName() { return applicationName; }
    public String getProfile() { return profile; }
    public Map<String, Object> getConfig() { return Map.copyOf(config); }
    public Map<String, Boolean> getFeatures() { return Map.copyOf(features); }
    public Map<String, String> getSecrets() { return Map.copyOf(secrets); }
    public Map<String, Object> getMetadata() { return Map.copyOf(metadata); }
    public long getTimestamp() { return timestamp; }
    
    // Package-private setters for builder
    void setConfig(Map<String, Object> config) {
        this.config.clear();
        this.config.putAll(config);
    }
    
    void setFeatures(Map<String, Boolean> features) {
        this.features.clear();
        this.features.putAll(features);
    }
    
    void setSecrets(Map<String, String> secrets) {
        this.secrets.clear();
        this.secrets.putAll(secrets);
    }
    
    void setMetadata(Map<String, Object> metadata) {
        this.metadata.clear();
        this.metadata.putAll(metadata);
    }
    
    /**
     * Configuration snapshot record for immutable data transfer
     */
    public record ConfigurationSnapshot(
        String applicationName,
        String profile,
        Map<String, Object> config,
        Map<String, Boolean> features,
        Map<String, String> secrets,
        Map<String, Object> metadata,
        long timestamp
    ) {
        public static ConfigurationSnapshot from(Configuration config) {
            return new ConfigurationSnapshot(
                config.applicationName,
                config.profile,
                Map.copyOf(config.config),
                Map.copyOf(config.features),
                Map.copyOf(config.secrets),
                Map.copyOf(config.metadata),
                config.timestamp
            );
        }
    }
    
    public ConfigurationSnapshot toSnapshot() {
        return ConfigurationSnapshot.from(this);
    }
}
