package com.example.controller;

import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config-debug")
@RefreshScope
public class ComprehensiveConfigController {

    private final Environment env;

    public ComprehensiveConfigController(Environment env) {
        this.env = env;
    }

    @Autowired
    private Unleash unleash;

    // GitHub Configuration Values
    @Value("${app.name:FALLBACK-APP-NAME}")
    private String appName;
    
    @Value("${app.version:0.0.0}")
    private String appVersion;
    
    @Value("${app.environment:fallback}")
    private String environment;

    // Feature Flags from GitHub
    @Value("${features.github-config:false}")
    private boolean githubConfigEnabled;
    
    @Value("${features.vault-secrets:false}")
    private boolean vaultSecretsEnabled;
    
    @Value("${features.user-management:false}")
    private boolean userManagementEnabled;

    // Vault Secrets
    @Value("${database.username:FALLBACK-DB-USER}")
    private String dbUsername;
    
    @Value("${database.password:FALLBACK-DB-PASSWORD}")
    private String dbPassword;
    
    @Value("${api.keys.weather:FALLBACK-WEATHER-KEY}")
    private String weatherApiKey;
    
    @Value("${external.oauth.clientId:FALLBACK-OAUTH-ID}")
    private String oauthClientId;

    @GetMapping("/all")
    public Map<String, Object> getAllConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        // Application Info (should come from GitHub)
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", appName);
        appInfo.put("version", appVersion);
        appInfo.put("environment", environment);
        appInfo.put("isFromGitHub", !appName.equals("FALLBACK-APP-NAME"));
        config.put("application", appInfo);
        
        // Feature Flags (should come from GitHub)
        Map<String, Object> features = new HashMap<>();
        features.put("newProfileFeature", unleash.isEnabled("new-profile-feature"));
        features.put("betaFeature", unleash.isEnabled("beta-feature"));
        config.put("features", features);
        
        // Secrets (should come from Vault)
        Map<String, Object> secrets = new HashMap<>();
        secrets.put("databaseUsername", dbUsername);
        secrets.put("databasePassword", maskSecret(dbPassword));
        secrets.put("isFromVault", !dbUsername.equals("FALLBACK-DB-USER"));
        config.put("secrets", secrets);
        
        return config;
    }

    @GetMapping("/debug")
    public Map<String, Object> getDebugInfo() {
        Map<String, Object> debug = new HashMap<>();
        
        debug.put("activeProfiles", String.join(",", env.getActiveProfiles()));
        debug.put("vaultEnabled", env.getProperty("spring.cloud.vault.enabled"));
        debug.put("configEnabled", env.getProperty("spring.cloud.config.enabled"));
        debug.put("dbUsername", env.getProperty("database.username"));
        debug.put("appName", env.getProperty("app.name"));
        
        return debug;
    }

    @GetMapping("/vault-test")
    public Map<String, Object> testVaultConnection() {
        Map<String, Object> vaultTest = new HashMap<>();
        
        boolean vaultWorking = !dbUsername.equals("fallback_user");
        vaultTest.put("vaultWorking", vaultWorking);
        vaultTest.put("vaultEnabled", env.getProperty("spring.vault.enabled"));
        
        if (vaultWorking) {
            vaultTest.put("sampleSecrets", Map.of(
                "dbUsername", dbUsername,
                "dbPassword", maskSecret(dbPassword)
                //"weatherKey", maskSecret(weatherApiKey)
            ));
        }
        
        return vaultTest;
    }
    
    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8 || secret.startsWith("FALLBACK")) {
            return secret.startsWith("FALLBACK") ? "NOT_LOADED" : "****";
        }
        return secret.substring(0, 3) + "****" + secret.substring(secret.length() - 3);
    }
}