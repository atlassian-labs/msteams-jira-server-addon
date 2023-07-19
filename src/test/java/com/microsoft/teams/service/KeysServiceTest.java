package com.microsoft.teams.service;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeysServiceTest {

    private KeysService keysService;

    @Before
    public void setUp() throws Exception {
        keysService = new KeysService();
    }

    @Test
    public void updateApplicationKeys() {
        Map<String, String> keys = spy(new HashMap<>());
        keys.put(CONSUMER_KEY, "consumer_key");
        keys.put(PRIVATE_KEY, "private_key");
        keys.put(PUBLIC_KEY, "public_key");
        keys.put(ATLAS_ID, "atlas_id");

        keysService.updateApplicationKeys(keys);
        verify(keys, times(8)).get(any());
    }

    @Test
    public void updateApplicationKeysIfNotExists() {
        Map<String, String> keys = spy(new HashMap<>());

        keysService.updateApplicationKeys(keys);
        verify(keys, times(4)).put(anyString(), anyString());
    }
}