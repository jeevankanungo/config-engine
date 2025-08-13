package com.example.servicea.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RefreshScope
public class ConfigController {

    // GitHub Configuration Values
    @Value("${app.name:FALLBACK-APP-NAME}")
    private String appName;

    @Value("${app.version:0.0.0}")
    private String appVersion;

    @Value("${app.environment:fallback}")
    private String environment;
    
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("name", appName);
        config.put("version", appVersion);
        config.put("environment", environment);
        //config.put("isFromGitHub", !appName.equals("FALLBACK-APP-NAME"));
        return config;
    }
    
    @GetMapping("/health")
    public String health() {
        return "Service A is healthy!";
    }
}
