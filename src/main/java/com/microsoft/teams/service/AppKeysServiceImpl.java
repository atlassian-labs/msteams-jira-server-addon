package com.microsoft.teams.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.microsoft.teams.ao.AppKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;

@Named
@Component
public class AppKeysServiceImpl implements AppKeysService {

    private static final Logger LOG = LoggerFactory.getLogger(AppKeysServiceImpl.class);

    @ComponentImport
    private final ActiveObjects activeObjects;

    @Inject
    public AppKeysServiceImpl(ActiveObjects activeObjects) {
        this.activeObjects = activeObjects;
    }

    @Override
    public void add(Map<String, String> keys) {
            AppKeys[] appKeysSet = activeObjects.find(AppKeys.class);
            AppKeys appKeys = (appKeysSet.length == 0) ? activeObjects.create(AppKeys.class) : appKeysSet[0];
            appKeys.setConsumerKey(keys.get(CONSUMER_KEY));
            appKeys.setPrivateKey(keys.get(PRIVATE_KEY));
            appKeys.setPublicKey(keys.get(PUBLIC_KEY));
            appKeys.setAtlasId(keys.get(ATLAS_ID));
            LOG.debug("Saving keys to AO transaction started. Consumer key - {}, Atlas id - {}, Public key - {}", appKeys.getConsumerKey(), appKeys.getAtlasId(), appKeys.getPublicKey());
            appKeys.save();
            LOG.debug("Saving keys to AO transaction performed");
    }

    @Override
    public Map<String, String> get() {
        Map<String, String> keys = new HashMap<>();
        AppKeys[] appKeys = activeObjects.find(AppKeys.class);
        if (appKeys.length != 0) {
            keys.put(CONSUMER_KEY, appKeys[0].getConsumerKey());
            keys.put(PRIVATE_KEY, appKeys[0].getPrivateKey());
            keys.put(PUBLIC_KEY, appKeys[0].getPublicKey());
            keys.put(ATLAS_ID, appKeys[0].getAtlasId());
        }
        String consumerKeyMsg = keys.containsKey(CONSUMER_KEY) ? "Consumer key = " + keys.get(CONSUMER_KEY) : "Consumer key is missing in AO";
        String atlasIdMsg = keys.containsKey(ATLAS_ID) ? "Atlas id = " + keys.get(ATLAS_ID) : "Atlas id is missing in AO";
        String publicKeyMsg = keys.containsKey(PUBLIC_KEY) ? "Public key = " + keys.get(PUBLIC_KEY) : "Public key is missing in AO";
        LOG.debug("Get data from AO. {}, {}, {}", consumerKeyMsg, atlasIdMsg, publicKeyMsg);
        return keys;
    }

    @Override
    public String getAtlasId() {
        AppKeys[] appKeys = activeObjects.find(AppKeys.class);
        if (appKeys.length != 0) {
            return appKeys[0].getAtlasId();
        }

        return null;
    }

    @Override
    public void delete() {
            List<AppKeys> appKeys = Arrays.asList(activeObjects.find(AppKeys.class));
            if (!appKeys.isEmpty()) {
                for (AppKeys appKey : appKeys) {
                    activeObjects.delete(appKey);
                }
            }
    }
}
