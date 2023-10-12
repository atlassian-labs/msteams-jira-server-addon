package com.microsoft.teams.service;

import com.atlassian.oauth.util.RSAKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.teams.oauth.PropertiesClient.*;

@Component
public class KeysService {

    private static final Logger LOG = LoggerFactory.getLogger(KeysService.class);

    private String publicKey;
    private String privateKey;
    private String atlasId;
    private String consumerKey;

    public void updateApplicationKeys(Map<String, String> properties) {
        String publicKeyBeforeUpdate = properties.get(PUBLIC_KEY);
        String privateKeyBeforeUpdate = properties.get(PRIVATE_KEY);
        if (publicKeyBeforeUpdate == null || publicKeyBeforeUpdate.isEmpty()
                || privateKeyBeforeUpdate == null || privateKeyBeforeUpdate.isEmpty()) {

            Map<String, String> keyPair = generateKeyPair();
            properties.put(PUBLIC_KEY, keyPair.get(PUBLIC_KEY));
            properties.put(PRIVATE_KEY, keyPair.get(PRIVATE_KEY));
        }

        String atlasIdBeforeUpdate = properties.get(ATLAS_ID);
        if (atlasIdBeforeUpdate == null || atlasIdBeforeUpdate.isEmpty()) {
            UUID randomUUID = UUID.randomUUID();
            properties.put(ATLAS_ID, randomUUID.toString());
        }

        String consumerKeyBeforeUpdate = properties.get(CONSUMER_KEY);
        if (consumerKeyBeforeUpdate == null || consumerKeyBeforeUpdate.isEmpty()) {
            properties.put(CONSUMER_KEY, CONSUMER_KEY_VALUE);
        }

        publicKey = properties.get(PUBLIC_KEY);
        privateKey = properties.get(PRIVATE_KEY);
        atlasId = properties.get(ATLAS_ID);
        consumerKey = properties.get(CONSUMER_KEY);
    }

    private Map<String, String> generateKeyPair() {
        Map<String, String> keyPair= new HashMap<>();
        try {
            KeyPair pair = RSAKeys.generateKeyPair();
            keyPair.put(PUBLIC_KEY, RSAKeys.toPemEncoding(pair.getPublic()));
            keyPair.put(PRIVATE_KEY, RSAKeys.toPemEncoding(pair.getPrivate()));
        } catch (Exception e) {
            LOG.debug("Generating key pair error (" + e.getClass().getCanonicalName() + "): " + e.getMessage());
        }
        return keyPair;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getAtlasId() {
        return atlasId;
    }

    public String getConsumerKey() {
        return consumerKey;
    }
}
