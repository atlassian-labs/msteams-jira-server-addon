package com.microsoft.teams.service.messaging;

import com.microsoft.teams.service.models.TeamsMessage;

public interface ProcessMessageStrategy {
    String processMessage(TeamsMessage message);
}
