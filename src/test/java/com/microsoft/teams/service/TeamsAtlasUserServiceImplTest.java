package com.microsoft.teams.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.microsoft.teams.ao.AppKeys;
import com.microsoft.teams.ao.TeamsAtlasUser;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(ActiveObjectsJUnitRunner.class)
public class TeamsAtlasUserServiceImplTest {

    @Inject
    private TeamsAtlasUserServiceImpl atlasUserService;
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
        keys.put(USER_NAME, "username");
        keys.put(ACCESS_TOKEN, "access_token");

        assertNotNull(entityManager);
        activeObjects = new TestActiveObjects(entityManager);
        atlasUserService = new TeamsAtlasUserServiceImpl(activeObjects);
    }

    @Test
    public void addingUserTest() {
        activeObjects.migrate(TeamsAtlasUser.class);
        assertTrue(atlasUserService.all().isEmpty());
        assertEquals(0, activeObjects.find(TeamsAtlasUser.class).length);

        atlasUserService.add(keys);
        activeObjects.flushAll(); // clear all caches

        final TeamsAtlasUser[] users = activeObjects.find(TeamsAtlasUser.class);
        assertEquals(1, users.length);
        assertEquals("username", users[0].getUserName());
        assertEquals("access_token", users[0].getAtlasAccessToken());
        assertEquals("123", users[0].getMsTeamsUserId());
    }

    @Test
    public void findingUserByTeamsIdTest() {
        activeObjects.migrate(TeamsAtlasUser.class);
        atlasUserService.add(keys);

        final List<TeamsAtlasUser> usersById = atlasUserService.getUserByTeamsId("123");

        activeObjects.flushAll();

        assertEquals(1, usersById.size());
        assertEquals("username", usersById.get(0).getUserName());
        assertEquals("access_token", usersById.get(0).getAtlasAccessToken());
        assertEquals("123", usersById.get(0).getMsTeamsUserId());

        atlasUserService.deleteAoObject("123");
        final List<TeamsAtlasUser> userShouldNotExists = atlasUserService.getUserByTeamsId("123");
        assertEquals(0, userShouldNotExists.size());
    }

    @Test
    public void testDeleteAoObject() {
        activeObjects.migrate(TeamsAtlasUser.class);
        atlasUserService.add(keys);

        atlasUserService.deleteAoObject("123");
        activeObjects.flushAll();

        final List<TeamsAtlasUser> userShouldNotExists = atlasUserService.getUserByTeamsId("123");
        assertEquals(0, userShouldNotExists.size());
    }

    @Test
    public void testDeleteAll() {
        activeObjects.migrate(TeamsAtlasUser.class);
        atlasUserService.add(keys);
        assertEquals(1, atlasUserService.all().size());

        atlasUserService.deleteAll();
        activeObjects.flushAll();

        assertEquals(0, atlasUserService.all().size());
    }

    @Test
    public void testAll() {
        activeObjects.migrate(TeamsAtlasUser.class);

        final TeamsAtlasUser user = activeObjects.create(TeamsAtlasUser.class);
        user.setUserName("username");
        user.setMsTeamsUserId("123");
        user.setAtlasAccessToken("access_token");
        user.setDateCreated(new Date());
        user.setDateUpdated(new Date());
        user.save();

        activeObjects.flushAll();

        final List<TeamsAtlasUser> all = atlasUserService.all();
        assertEquals(1, all.size());
    }

    @Test
    public void findingUserByUsernameTest() {
        activeObjects.migrate(TeamsAtlasUser.class);
        atlasUserService.add(keys);

        final List<TeamsAtlasUser> usersByUsername = atlasUserService.getUserByUserName("username");

        assertEquals(1, usersByUsername.size());
        assertEquals("username", usersByUsername.get(0).getUserName());
        assertEquals("access_token", usersByUsername.get(0).getAtlasAccessToken());
        assertEquals("123", usersByUsername.get(0).getMsTeamsUserId());

        activeObjects.flushAll();
    }
}