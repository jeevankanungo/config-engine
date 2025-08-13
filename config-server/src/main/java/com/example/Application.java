package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.vault.config.VaultAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {
    VaultAutoConfiguration.class
})
@EnableConfigServer
@Import(VaultConditionalConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@ConditionalOnProperty(name = "vault.enabled", havingValue = "true")
@Import({
    VaultAutoConfiguration.class
})
class VaultConditionalConfiguration {
    // Conditional Vault configuration
}