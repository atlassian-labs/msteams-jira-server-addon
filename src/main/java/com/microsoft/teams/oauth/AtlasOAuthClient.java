package com.microsoft.teams.oauth;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.microsoft.teams.service.HostPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Component
public class AtlasOAuthClient {

    private final OAuthTokenFactory oAuthGetAccessTokenFactory;
    private final HostPropertiesService hostProperties;
    private static final String AUTHORIZATION_URL = "%s/plugins/servlet/oauth/authorize";

    @Autowired
    public AtlasOAuthClient(OAuthTokenFactory oAuthGetAccessTokenFactory,
                            HostPropertiesService hostProperties) {
        this.oAuthGetAccessTokenFactory = oAuthGetAccessTokenFactory;
        this.hostProperties = hostProperties;
    }

    /**
     * Gets temporary request token and creates url to authorize it
     *
     * @param consumerKey consumer key
     * @param privateKey  private key in PKCS8 format
     * @return request token value
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public String getAndAuthorizeTemporaryTokenWithUrl(String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        AtlasOAuthGetTemporaryToken temporaryToken = oAuthGetAccessTokenFactory.getTemporaryToken(consumerKey, privateKey);
        OAuthCredentialsResponse response = temporaryToken.execute();


        OAuthAuthorizeTemporaryTokenUrl authorizationURL = new OAuthAuthorizeTemporaryTokenUrl(String.format(AUTHORIZATION_URL, hostProperties.getFullBaseUrl()));
        authorizationURL.temporaryToken = response.token;
        
        return authorizationURL.toString();
    }
    
    /**
     * Gets acces token from Atlassian product
     *
     * @param tmpToken    temporary request token
     * @param secret      secret (verification code provided by Atlassian product after request token authorization)
     * @param consumerKey consumer ey
     * @param privateKey  private key in PKCS8 format
     * @return access token value
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public String getAccessToken(String tmpToken, String secret, String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        AtlasOAuthGetAccessToken oAuthAccessToken = oAuthGetAccessTokenFactory.getAtlasOAuthGetAccessToken(tmpToken, secret, consumerKey, privateKey);
        OAuthCredentialsResponse response = oAuthAccessToken.execute();

        return response.token;
    }

    /**
     * Creates OAuthParameters used to make authorized request to Atlassian product
     *
     * @param tmpToken
     * @param secret
     * @param consumerKey
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public OAuthParameters getParameters(String tmpToken, String secret, String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        AtlasOAuthGetAccessToken oAuthAccessToken = oAuthGetAccessTokenFactory.getAtlasOAuthGetAccessToken(tmpToken, secret, consumerKey, privateKey);
        oAuthAccessToken.verifier = secret;
        return oAuthAccessToken.createParameters();
    }
}
