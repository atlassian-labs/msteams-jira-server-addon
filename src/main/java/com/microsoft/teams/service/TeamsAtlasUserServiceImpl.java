package com.microsoft.teams.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.microsoft.teams.ao.AppKeys;
import com.microsoft.teams.ao.TeamsAtlasUser;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.microsoft.teams.oauth.PropertiesClient.*;

@Named
@Component
public class TeamsAtlasUserServiceImpl implements TeamsAtlasUserService {

    private static final Logger LOG = LoggerFactory.getLogger(TeamsAtlasUserServiceImpl.class);

    @ComponentImport
    private final ActiveObjects activeObjects;

    @Inject
    public TeamsAtlasUserServiceImpl(ActiveObjects activeObjects) {
        this.activeObjects = activeObjects;
    }

    @Override
    public void add(Map<String, String> properties) {
            final TeamsAtlasUser user;
            List<TeamsAtlasUser> usersByTeamsId = getUserByTeamsId(properties.get(TEAMS_ID));
            if (usersByTeamsId.isEmpty()) {
                user = activeObjects.create(TeamsAtlasUser.class);
                setUserProperties(user, properties);
                user.setDateCreated(new Date());
            } else {
                user = retrieveFirstTeamsAtlasUser(usersByTeamsId);
                setUserProperties(user, properties);
            }
            user.save();
            LOG.info("Teams user with id: {} has been saved to database", user.getMsTeamsUserId());
    }

    private void setUserProperties(TeamsAtlasUser user, Map<String, String> properties) {
        user.setAtlasAccessToken(properties.get(ACCESS_TOKEN));
        // Always writing Teams id in lowercase to ensure exact match "get user by id" query succeeds
        user.setMsTeamsUserId(properties.get(TEAMS_ID).toLowerCase());
        user.setDateUpdated(new Date());
        user.setUserName(properties.get(USER_NAME));
    }

    private TeamsAtlasUser retrieveFirstTeamsAtlasUser(List<TeamsAtlasUser> usersByTeamsId) {
        return usersByTeamsId.get(0);
    }

    @Override
    public void updateDbToAoObjects() {
            activeObjects.migrateDestructively(AppKeys.class, TeamsAtlasUser.class);
    }

    @Override
    public List<TeamsAtlasUser> all() {
        return Arrays.asList(activeObjects.find(TeamsAtlasUser.class));
    }

    @Override
    public List<TeamsAtlasUser> getUserByTeamsId(String teamsId) {
        try {
            return Arrays.asList(activeObjects.find(TeamsAtlasUser.class,
                    Query.select().where("MS_TEAMS_USER_ID = LOWER(?)", teamsId)));
        } catch (Exception exception) {
            LOG.info("Error in database query: {}", exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<TeamsAtlasUser> getUserByUserName(String userName) {
        try {
            return Arrays.asList(activeObjects.find(TeamsAtlasUser.class,
                    Query.select().where("USER_NAME = LOWER(?)", userName)));
        } catch (Exception exception) {
            LOG.info("Error in database query: {}", exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteAoObject(String msTeamsUserId) {
            List<TeamsAtlasUser> users = getUserByTeamsId(msTeamsUserId);
            if (!users.isEmpty()) {
                activeObjects.delete(retrieveFirstTeamsAtlasUser(users));
            }
    }

    @Override
    public void deleteAll() {
            TeamsAtlasUser[] users = activeObjects.find(TeamsAtlasUser.class);
            if (users != null)
                activeObjects.delete(users);
    }
}
