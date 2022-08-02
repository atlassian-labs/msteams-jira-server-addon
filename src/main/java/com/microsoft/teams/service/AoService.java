package com.microsoft.teams.service;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface AoService {
    void updateDbToAoObjects();
}
