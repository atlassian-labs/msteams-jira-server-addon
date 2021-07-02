package com.microsoft.teams.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.microsoft.teams.ao.TeamsAtlasUser;

import java.util.List;
import java.util.Map;

@Transactional
public interface TeamsAtlasUserService {
    void add(Map<String, String> properties);

    List<TeamsAtlasUser> all();

    List<TeamsAtlasUser> getUserByTeamsId(String teamsId);

    List<TeamsAtlasUser> getUserByUserName(String userName);

    void deleteAoObject(String msTeamsUserId);

    void deleteAll();

    void updateDbToAoObjects();
}
