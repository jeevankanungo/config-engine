package com.example.serviceb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RefreshScope
public class ConfigController {
    
    @Value("${service.message:Default message}")
    private String message;
    
    @Value("${service.secret:No secret}")
    private String secret;
    
    @Value("${api.key:No API key}")
    private String apiKey;
    
    @Value("${api.endpoint:No endpoint}")
    private String apiEndpoint;
    
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("service", "Service B");
        config.put("message", message);
        config.put("secret", secret);
        config.put("apiKey", apiKey);
        config.put("apiEndpoint", apiEndpoint);
        return config;
    }
    
    @GetMapping("/health")
    public String health() {
        return "Service B is healthy!";
    }
}
