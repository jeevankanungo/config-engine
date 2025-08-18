package com.example.configclient.service;

import com.example.configclient.config.AppConfig;
import com.example.configclient.model.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Configuration> configCache;

    public ConfigService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.getConfigServerTimeout()))
                .build();
        this.objectMapper = new ObjectMapper();
        this.configCache = new ConcurrentHashMap<>();
    }

    public Configuration fetchConfigurationFromServer(String applicationName, String profile) {
        try {
            String url = String.format("%s/%s/%s", AppConfig.getConfigServerUrl(), applicationName, profile);
            logger.info("Fetching configuration from: {}", url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(AppConfig.getConfigServerTimeout()))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Configuration config = objectMapper.readValue(response.body(), Configuration.class);
                logger.info("Successfully fetched configuration for {}-{}", applicationName, profile);
                return config;
            } else {
                logger.warn("HTTP {} received from config server for {}-{}", 
                           response.statusCode(), applicationName, profile);
                return null;
            }
            
        } catch (IOException e) {
            logger.error("IO error while fetching configuration for {}-{}: {}", 
                        applicationName, profile, e.getMessage());
            return null;
        } catch (InterruptedException e) {
            logger.error("Request interrupted while fetching configuration for {}-{}", 
                        applicationName, profile);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching configuration for {}-{}: {}", 
                        applicationName, profile, e.getMessage(), e);
            return null;
        }
    }

    public void updateConfigInMemory(String key, Configuration configuration) {
        if (configuration != null) {
            configCache.put(key, configuration);
            logger.info("Configuration updated in memory for key: {}", key);
        }
    }

    public Configuration getConfigFromMemory(String key) {
        return configCache.get(key);
    }

    public Map<String, Configuration> getAllConfigurations() {
        return Map.copyOf(configCache);
    }

    public void clearCache() {
        configCache.clear();
        logger.info("Configuration cache cleared");
    }

    public boolean hasConfiguration(String key) {
        return configCache.containsKey(key);
    }

    public int getCacheSize() {
        return configCache.size();
    }
}
