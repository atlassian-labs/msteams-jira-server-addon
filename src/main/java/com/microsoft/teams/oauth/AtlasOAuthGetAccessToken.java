package com.microsoft.teams.oauth;

public class AtlasOAuthGetAccessToken extends com.google.api.client.auth.oauth.OAuthGetAccessToken {

    /**
     * @param authorizationServerUrl encoded authorization server URL
     */
    public AtlasOAuthGetAccessToken(String authorizationServerUrl) {
        super(authorizationServerUrl);
        this.usePost = true;
    }

}
