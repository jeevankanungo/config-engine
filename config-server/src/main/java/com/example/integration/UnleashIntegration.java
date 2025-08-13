package com.example.integration;

import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UnleashIntegration {
    
    @Autowired
    private Unleash unleash;
    
    @Cacheable(value = "featureFlags", key = "#applicationName + '-' + #profile")
    public Map<String, Boolean> getFeatureFlags(String applicationName, String profile) {
        Map<String, Boolean> features = new HashMap<>();
        
        UnleashContext context = UnleashContext.builder()
            //.appName(applicationName)
            //.environment(profile)
            .build();
        
        // Get all feature flags for the application
        List<String> featureNames = getFeatureNamesForApplication(applicationName);
        
        for (String featureName : featureNames) {
            features.put(featureName, unleash.isEnabled(featureName, context));
        }
        
        return features;
    }
    
    private List<String> getFeatureNamesForApplication(String applicationName) {
        // Define feature flags per application
        if ("service-a".equals(applicationName)) {
            return List.of(
                "service-a.new-feature",
                "service-a.beta-feature",
                "service-a.dark-mode",
                "service-a.advanced-analytics",
                "service-a.use-new-algorithm",
                "service-a.enhanced-processing",
                "service-a.caching-enabled",
                "service-a.rate-limiting"
            );
        } else if ("service-b".equals(applicationName)) {
            return List.of(
                "service-b.caching",
                "service-b.rate-limit",
                "service-b.new-endpoint",
                "service-b.v2-api",
                "service-b.async-processing",
                "service-b.batch-mode",
                "service-b.premium-features",
                "service-b.debug-mode"
            );
        }
        else if ("spring-cloud-integration".equals(applicationName)) {
            return List.of(
                    "new-profile-feature",
                    "beta-feature"
            );
        }
        return List.of();
    }
}
