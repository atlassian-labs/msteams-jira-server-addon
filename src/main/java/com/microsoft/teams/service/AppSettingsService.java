package com.microsoft.teams.service;

import com.atlassian.activeobjects.tx.Transactional;

import java.util.Map;

@Transactional
public interface AppSettingsService {
    void set(Map<String, String> keys);

    Map<String, String> get();
}
