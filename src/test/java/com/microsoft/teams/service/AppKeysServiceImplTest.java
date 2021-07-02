package com.microsoft.teams.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.microsoft.teams.ao.AppKeys;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;
import static org.junit.Assert.*;

@RunWith(ActiveObjectsJUnitRunner.class)
public class AppKeysServiceImplTest {

    @Inject
    private AppKeysServiceImpl appKeysService;
    private ActiveObjects activeObjects;
    private EntityManager entityManager;
    Map<String, String> keys;

    @Before
    public void setUp() throws Exception {
        keys = new HashMap<>();
        keys.put(CONSUMER_KEY, "consumer_key");
        keys.put(PRIVATE_KEY, "private_key");
        keys.put(PUBLIC_KEY, "public_key");
        keys.put(ATLAS_ID, "atlas_id");
        keys.put(TEAMS_ID, "123");
        keys.put(USER_NAME, "Username");
        keys.put(ACCESS_TOKEN, "access_token");

        assertNotNull(entityManager);
        activeObjects = new TestActiveObjects(entityManager);
        appKeysService = new AppKeysServiceImpl(activeObjects);
    }

    @Test
    public void addAppKeysTest() {
        activeObjects.migrate(AppKeys.class);
        assertTrue(appKeysService.get().isEmpty());
        assertEquals(0, activeObjects.find(AppKeys.class).length);

        appKeysService.add(keys);
        assertEquals(1, activeObjects.find(AppKeys.class).length);
        activeObjects.flushAll();
    }

    @Test
    public void getAppKeysTest() {
        activeObjects.migrate(AppKeys.class);
        assertTrue(appKeysService.get().isEmpty());
        assertEquals(0, appKeysService.get().size());
        assertNotNull(appKeysService.get());

        appKeysService.add(keys);
        assertEquals(4, appKeysService.get().size());
        activeObjects.flushAll();
    }

    @Test
    public void getAtlasIdTest() {
        activeObjects.migrate(AppKeys.class);
        assertNull(appKeysService.getAtlasId());
        appKeysService.add(keys);
        assertNotNull(appKeysService.getAtlasId());

        activeObjects.flushAll();
    }

    @Test
    public void deleteAppKeysTest() {
        activeObjects.migrate(AppKeys.class);
        appKeysService.add(keys);
        assertNotNull(appKeysService.get());

        appKeysService.delete();
        assertEquals(0, appKeysService.get().size());
        activeObjects.flushAll();
    }
}