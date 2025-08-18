package com.example.configclient;

import com.example.configclient.cache.ConfigCache;
import com.example.configclient.cache.InMemoryConfigCache;
import com.example.configclient.http.BasicAuthHttpClient;
import com.example.configclient.http.HttpClient;

import java.time.Duration;
import java.util.Objects;

/**
 * Builder for ConfigClient using Java 17 features
 */
public class ConfigClientBuilder {
    
    private String configServerUrl = "http://localhost:8888";
    private String applicationName;
    private String profile = "default";
    private String username;
    private String password;
    private Duration connectionTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
    private long retryDelayMs = 1000;
    private boolean autoRefresh = false;
    private long autoRefreshIntervalMinutes = 5;
    private ConfigCache cache;
    private HttpClient httpClient;
    
    public ConfigClientBuilder configServerUrl(String url) {
        this.configServerUrl = Objects.requireNonNull(url, "Config server URL cannot be null");
        return this;
    }
    
    public ConfigClientBuilder applicationName(String name) {
        this.applicationName = Objects.requireNonNull(name, "Application name cannot be null");
        return this;
    }
    
    public ConfigClientBuilder profile(String profile) {
        this.profile = Objects.requireNonNullElse(profile, "default");
        return this;
    }
    
    public ConfigClientBuilder credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }
    
    public ConfigClientBuilder connectionTimeout(Duration timeout) {
        this.connectionTimeout = Objects.requireNonNull(timeout);
        return this;
    }
    
    public ConfigClientBuilder readTimeout(Duration timeout) {
        this.readTimeout = Objects.requireNonNull(timeout);
        return this;
    }
    
    public ConfigClientBuilder maxRetries(int retries) {
        if (retries < 1) {
            throw new IllegalArgumentException("Max retries must be at least 1");
        }
        this.maxRetries = retries;
        return this;
    }
    
    public ConfigClientBuilder retryDelay(long delayMs) {
        if (delayMs < 0) {
            throw new IllegalArgumentException("Retry delay cannot be negative");
        }
        this.retryDelayMs = delayMs;
        return this;
    }
    
    public ConfigClientBuilder enableAutoRefresh(long intervalMinutes) {
        if (intervalMinutes < 1) {
            throw new IllegalArgumentException("Auto refresh interval must be at least 1 minute");
        }
        this.autoRefresh = true;
        this.autoRefreshIntervalMinutes = intervalMinutes;
        return this;
    }
    
    public ConfigClientBuilder withCache(ConfigCache cache) {
        this.cache = Objects.requireNonNull(cache, "Cache cannot be null");
        return this;
    }
    
    public ConfigClientBuilder withHttpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client cannot be null");
        return this;
    }
    
    public ConfigClient build() {
        Objects.requireNonNull(applicationName, "Application name is required");
        
        if (cache == null) {
            cache = new InMemoryConfigCache();
        }
        
        if (httpClient == null) {
            httpClient = new BasicAuthHttpClient(
                username, 
                password, 
                connectionTimeout, 
                readTimeout
            );
        }
        
        var client = new ConfigClientImpl(
            configServerUrl,
            applicationName,
            profile,
            httpClient,
            cache,
            maxRetries,
            retryDelayMs
        );
        
        if (autoRefresh) {
            client.startAutoRefresh(autoRefreshIntervalMinutes);
        }
        
        return client;
    }
}
