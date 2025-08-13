package com.example.controller;

import com.example.service.ConfigService;
import io.getunleash.Unleash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private ConfigService configService;

    @Autowired
    private Unleash unleash;

    @Value("${app.message:Default message}")
    private String message;

    @Value("${unleash.enabled:false}")
    private boolean unleashEnabled;

    @GetMapping("/public/health")
    public Map<String, Object> publicHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", message);
        response.put("unleashEnabled", unleashEnabled);
        response.put("unleashType", unleash.getClass().getSimpleName());
        return response;
    }

    @GetMapping("/secure/profile")
    public Map<String, Object> secureProfile(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("config", configService.getConfigFromVault());
        
        // Feature flag example - works with both FakeUnleash and real Unleash
        response.put("newFeatureEnabled", unleash.isEnabled("new-profile-feature"));
        response.put("unleashType", unleash.getClass().getSimpleName());
        
        return response;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfiguration() {
        Map<String, Object> response = new HashMap<>();
        response.put("vaultConfig", configService.getConfigFromVault());
        response.put("unleashEnabled", unleashEnabled);
        response.put("unleashType", unleash.getClass().getSimpleName());
        response.put("featureFlags", Map.of(
            "newProfileFeature", unleash.isEnabled("new-profile-feature"),
            "betaFeature", unleash.isEnabled("beta-feature")
        ));
        return response;
    }
}