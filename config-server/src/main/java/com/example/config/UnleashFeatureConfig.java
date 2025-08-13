package com.example.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.FakeUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class UnleashFeatureConfig {

    private static final Logger logger = LoggerFactory.getLogger(UnleashFeatureConfig.class);

    @Value("${unleash.api.url:http://localhost:4242/api}")
    private String unleashApiUrl;

    @Value("${unleash.api.token:default}")
    private String unleashApiToken;

    @Value("${unleash.environment:development}")
    private String environment;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "unleash.enabled", havingValue = "false", matchIfMissing = true)
    public Unleash fakeUnleash() {
        logger.info("Using FakeUnleash - no connection to Unleash server will be made");
        return new FakeUnleash();
    }

    @Bean
    @ConditionalOnProperty(name = "unleash.enabled", havingValue = "true")
    public Unleash realUnleash() {
        logger.info("Initializing real Unleash with URL: {}", unleashApiUrl);
        
        UnleashConfig config = UnleashConfig.builder()
            .appName("spring-cloud-integration")
            .instanceId("instance-1")
            .environment(environment)
            .unleashAPI(unleashApiUrl)
            .apiKey(unleashApiToken)
            .fetchTogglesInterval(30)
            .sendMetricsInterval(60)
            .synchronousFetchOnInitialisation(false)
            .build();

        return new DefaultUnleash(config);
    }

    @Bean
    @Profile("test")
    public Unleash testUnleash() {
        return new FakeUnleash();
    }
}