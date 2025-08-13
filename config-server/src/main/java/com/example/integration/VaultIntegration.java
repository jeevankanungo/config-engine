package com.example.integration;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VaultIntegration {

    private static final Logger logger = LoggerFactory.getLogger(VaultIntegration.class);

    @Autowired(required = true)
    private Vault vault;

    @Cacheable(value = "vaultSecrets", key = "#applicationName + '-' + #profile")
    public Map<String, Object> getSecrets(String applicationName, String profile) {
        Map<String, Object> secrets = new HashMap<>();

        if (vault == null) {
            logger.warn("Vault client is not configured, returning empty secrets");
            return secrets;
        }

        try {
            // Read secrets from Vault
            String path = String.format("secret/%s/%s", applicationName, profile);
            System.out.println("path" +path);
            logger.debug("Reading secrets from Vault path: {}", path);

            LogicalResponse response = vault.logical().read(path);

            if (response != null && response.getData() != null) {
                // Vault v2 KV store returns data under "data" key
                System.out.println("response" +response.getData());
                Object dataObj = response.getData();
                if (dataObj instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    secrets.putAll(data);
                    logger.debug("Loaded {} secrets for {}/{}", data.size(), applicationName, profile);
                }
            } else {
                logger.debug("No secrets found at path: {}", path);
            }

            // Also read common secrets
            String commonPath = String.format("secret/data/common/%s", profile);
            logger.debug("Reading common secrets from Vault path: {}", commonPath);

            LogicalResponse commonResponse = vault.logical().read(commonPath);

            if (commonResponse != null && commonResponse.getData() != null) {
                Object commonDataObj = commonResponse.getData().get("data");
                if (commonDataObj instanceof Map) {
                    Map<String, Object> commonData = (Map<String, Object>) commonDataObj;
                    secrets.putAll(commonData);
                    logger.debug("Loaded {} common secrets for profile {}", commonData.size(), profile);
                }
            }

        } catch (VaultException e) {
            logger.error("Error reading secrets from Vault for {}/{}: {}",
                    applicationName, profile, e.getMessage());
            // Return empty secrets on error rather than failing
        } catch (Exception e) {
            logger.error("Unexpected error reading secrets from Vault: {}", e.getMessage());
        }

        return secrets;
    }
}