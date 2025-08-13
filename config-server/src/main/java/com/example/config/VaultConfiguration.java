package com.example.config;

import com.bettercloud.vault.VaultConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
@ConditionalOnProperty(name = "vault.enabled", havingValue = "true")
public class VaultConfiguration {

    @Value("${vault.address:http://vault:8200}")
    private String vaultAddress;

    //@Value("${vault.token}")
    //private String vaultToken;

    @Bean
    public Vault vault() throws VaultException {
        VaultConfig config = new VaultConfig()
                .address("http://localhost:8200")
                .token("myroot")
                .build();

        return new Vault(config);
    }
}