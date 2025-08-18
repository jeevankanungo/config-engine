package com.example.configclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {
    private String version;
    private Map<String, Object> properties;
    private LocalDateTime lastUpdated;
    private String environment;

    public Configuration() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Configuration(String version, Map<String, Object> properties, String environment) {
        this.version = version;
        this.properties = properties;
        this.environment = environment;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(version, that.version) &&
               Objects.equals(properties, that.properties) &&
               Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, properties, environment);
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "version='" + version + '\'' +
                ", properties=" + properties +
                ", lastUpdated=" + lastUpdated +
                ", environment='" + environment + '\'' +
                '}';
    }
}
