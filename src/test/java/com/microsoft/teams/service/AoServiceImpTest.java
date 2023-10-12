package com.microsoft.teams.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.microsoft.teams.ao.AppKeys;
import com.microsoft.teams.ao.AppSettings;
import com.microsoft.teams.ao.TeamsAtlasUser;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(ActiveObjectsJUnitRunner.class)
public class AoServiceImpTest {
    @Inject
    private AoServiceImpl aoService;
    private ActiveObjects activeObjects;
    private EntityManager entityManager;
    Map<String, String> keys;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        activeObjects = new TestActiveObjects(entityManager);
        aoService = new AoServiceImpl(activeObjects);
    }

    @Test
    public void updateDbToAoObjects() {
        ActiveObjects activeObjects = mock(ActiveObjects.class);
        aoService = new AoServiceImpl(activeObjects);
        doNothing().when(activeObjects).migrateDestructively(AppKeys.class, TeamsAtlasUser.class);
        aoService.updateDbToAoObjects();
        verify(activeObjects).migrateDestructively(AppKeys.class, TeamsAtlasUser.class, AppSettings.class);
    }
}
