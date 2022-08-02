package com.microsoft.teams.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.microsoft.teams.ao.AppKeys;
import com.microsoft.teams.ao.AppSettings;
import com.microsoft.teams.ao.TeamsAtlasUser;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Component
public class AoServiceImpl implements AoService {
    @ComponentImport
    private final ActiveObjects activeObjects;

    @Inject
    public AoServiceImpl(ActiveObjects activeObjects) {
        this.activeObjects = activeObjects;
    }

    @Override
    public void updateDbToAoObjects() {
        activeObjects.migrateDestructively(AppKeys.class, TeamsAtlasUser.class, AppSettings.class);
    }
}
