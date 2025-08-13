package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigService {

    @Value("${vault.database.username:demo-user}")
    private String dbUsername;

    @Value("${vault.database.password:demo-password}")
    private String dbPassword;

    @Value("${api.key:demo-api-key}")
    private String apiKey;

    @Value("${vault.external.service.url:https://api.demo.com}")
    private String externalServiceUrl;

    @Value("${vault.enabled:false}")
    private boolean vaultEnabled;

    public Map<String, String> getConfigFromVault() {
        Map<String, String> config = new HashMap<>();
        
        if (vaultEnabled) {
            config.put("database.username", dbUsername);
            config.put("database.password", maskSecret(dbPassword));
            config.put("api.key", maskSecret(apiKey));
            config.put("external.service.url", externalServiceUrl);
            config.put("vault.status", "ENABLED - Loading secrets from Vault");
        } else {
            config.put("database.username", "demo-user (fallback)");
            config.put("database.password", "****");
            config.put("api.key", "****");
            config.put("external.service.url", "https://api.demo.com (fallback)");
            config.put("vault.status", "DISABLED - using fallback values");
        }
        
        return config;
    }
    
    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 4) {
            return "****";
        }
        return secret.substring(0, 2) + "****" + secret.substring(secret.length() - 2);
    }
}