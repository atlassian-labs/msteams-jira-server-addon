package com.microsoft.teams.service;

import com.atlassian.activeobjects.tx.Transactional;

import java.util.Map;

@Transactional
public interface AppKeysService {
    void add(Map<String, String> keys);

    Map<String, String> get();

    String getAtlasId();

    void delete();
}
