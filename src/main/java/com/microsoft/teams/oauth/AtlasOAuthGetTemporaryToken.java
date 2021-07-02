package com.microsoft.teams.oauth;

public class AtlasOAuthGetTemporaryToken extends com.google.api.client.auth.oauth.OAuthGetTemporaryToken {

    /**
     * @param authorizationServerUrl encoded authorization server URL
     */
    public AtlasOAuthGetTemporaryToken(String authorizationServerUrl) {
        super(authorizationServerUrl);
        this.usePost = true;
    }

}
