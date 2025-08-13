package com.example.controller;

import com.example.integration.*;
//import com.example.integration.VaultIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentController;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("{application}/{profile}")
public class CustomConfigController {
    
    @Autowired
    private EnvironmentController environmentController;
    
    @Autowired
    private UnleashIntegration unleashIntegration;

    @Autowired
    private EnvironmentRepository environmentRepository;
    
    @Autowired
    private VaultIntegration vaultIntegration;
    
    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> getCompleteConfig(
            @PathVariable String application,
            @PathVariable String profile,
            @RequestParam(required = false,defaultValue = "main") String label) {
        
        Map<String, Object> completeConfig = new HashMap<>();
        
        // Get configuration from Git
        //Environment env = environmentController.defaultLabel(application, profile);


        Environment env = environmentRepository.findOne(application,profile,label);

        Map<String, Object> gitConfig = new HashMap<>();
        for (PropertySource source : env.getPropertySources()) {
            gitConfig.putAll((Map<? extends String, ?>) source.getSource());

        }
        completeConfig.put("config", gitConfig);
        
        // Get feature flags from Unleash
        Map<String, Boolean> features = unleashIntegration.getFeatureFlags(application, profile);
        completeConfig.put("features", features);
        
        // Get secrets from Vault
        Map<String, Object> secrets = vaultIntegration.getSecrets(application, profile);
        completeConfig.put("secrets", secrets);
        
        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("application", application);
        metadata.put("profile", profile);
        metadata.put("label", label != null ? label : "main");
        metadata.put("timestamp", System.currentTimeMillis());
        completeConfig.put("metadata", metadata);
        
        return ResponseEntity.ok(completeConfig);
    }
    
    @GetMapping("/features")
    public ResponseEntity<Map<String, Boolean>> getFeatures(
            @PathVariable String application,
            @PathVariable String profile) {
        
        Map<String, Boolean> features = unleashIntegration.getFeatureFlags(application, profile);
        return ResponseEntity.ok(features);
    }
    
    @GetMapping("/secrets")
    public ResponseEntity<Map<String, Object>> getSecrets(
            @PathVariable String application,
            @PathVariable String profile) {
        
        Map<String, Object> secrets = vaultIntegration.getSecrets(application, profile);
        return null;//ResponseEntity.ok(secrets);
    }
}
