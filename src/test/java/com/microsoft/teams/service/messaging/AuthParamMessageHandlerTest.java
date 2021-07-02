package com.microsoft.teams.service.messaging;

import com.microsoft.teams.oauth.AtlasOAuthClient;
import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.KeysService;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;
import org.jose4j.jwt.GeneralJwtException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

public class AuthParamMessageHandlerTest {

    @Mock
    private PropertiesClient propertiesClient;
    @Mock
    private AtlasOAuthClient atlasOAuthClient;
    @Mock
    private ImageHelper imageHelper;
    @Mock
    private KeysService keysService;
    @Mock
    private AppKeysService appKeysService;
    @Mock
    private RequestService requestService;
    private AuthParamMessageHandler spyMessageHandler;
    private TeamsMessage validTeamsMessage;

    @Before
    public void setUp() throws GeneralJwtException {
        MockitoAnnotations.initMocks(this);
        String validTeamsMessageJson = "{\"requestToken\":\"RToken\",\"teamsId\":\"c2d0390e-4395-4802-b98b-ac3c88aa8779\"," +
                "\"verificationCode\":\"someCode\",\"token\":\"sOmE_vAlId_ToKeN\"}";
        validTeamsMessage = new TeamsMessageCreatorImpl().create(validTeamsMessageJson);
        spyMessageHandler = new AuthParamMessageHandler(propertiesClient, atlasOAuthClient, imageHelper, keysService, appKeysService, requestService);
    }

    @Test
    public void processMessage() {
        doNothing().when(propertiesClient).saveUserToDatabase(anyMap());

        spyMessageHandler.processMessage(validTeamsMessage);

        verify(propertiesClient, times(2)).saveUserToDatabase(anyMap());
    }
}