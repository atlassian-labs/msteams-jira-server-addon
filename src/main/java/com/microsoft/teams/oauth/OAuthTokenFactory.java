package com.microsoft.teams.oauth;

import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.microsoft.teams.service.HostPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
public class OAuthTokenFactory {
    private static final String ACCESS_TOKEN_URL_PATH = "%s/plugins/servlet/oauth/access-token";
    private static final String REQUEST_TOKEN_URL_PATH = "%s/plugins/servlet/oauth/request-token";
    private final HostPropertiesService hostProperties;

    @Autowired
    OAuthTokenFactory(HostPropertiesService hostProperties) {
        this.hostProperties = hostProperties;
    }

    /**
     * Initialize AtlasOAuthGetAccessToken
     * by setting it to use POST method, secret, request token
     * and setting consumer and private keys.
     *
     * @param tmpToken    request token
     * @param secret      secret (verification code provided by Atlassian product after request token authorization)
     * @param consumerKey consumer ey
     * @param privateKey  private key in PKCS8 format
     * @return AtlasOAuthGetAccessToken request
     * @throws NoSuchAlgorithmException from getOAuthRsaSigner() method
     * @throws InvalidKeySpecException from getOAuthRsaSigner() method
     */
    AtlasOAuthGetAccessToken getAtlasOAuthGetAccessToken(String tmpToken, String secret, String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        AtlasOAuthGetAccessToken accessToken = new AtlasOAuthGetAccessToken(String.format(ACCESS_TOKEN_URL_PATH, hostProperties.getFullBaseUrl()));
        accessToken.consumerKey = consumerKey;
        accessToken.signer = getOAuthRsaSigner(privateKey);
        accessToken.transport = new ApacheHttpTransport();
        accessToken.verifier = secret;
        accessToken.temporaryToken = tmpToken;
        return accessToken;
    }


    /**
     * Initialize AtlasOAuthGetTemporaryToken
     * by setting it to use POST method, oob (Out of Band) callback
     * and setting consumer and private keys.
     *
     * @param consumerKey consumer key
     * @param privateKey  private key in PKCS8 format
     * @return AtlasOAuthGetTemporaryToken request
     * @throws NoSuchAlgorithmException from getOAuthRsaSigner() method
     * @throws InvalidKeySpecException from getOAuthRsaSigner() method
     */
    AtlasOAuthGetTemporaryToken getTemporaryToken(String consumerKey, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        AtlasOAuthGetTemporaryToken atlasOAuthGetTemporaryToken = new AtlasOAuthGetTemporaryToken(String.format(REQUEST_TOKEN_URL_PATH, hostProperties.getFullBaseUrl()));
        atlasOAuthGetTemporaryToken.consumerKey = consumerKey;
        atlasOAuthGetTemporaryToken.signer = getOAuthRsaSigner(privateKey);
        atlasOAuthGetTemporaryToken.transport = new ApacheHttpTransport();
        atlasOAuthGetTemporaryToken.callback = "oob";
        return atlasOAuthGetTemporaryToken;
    }

    /**
     * @param privateKey private key in PKCS8 format
     * @return OAuthRsaSigner
     * @throws NoSuchAlgorithmException from getPrivateKey() method
     * @throws InvalidKeySpecException from getPrivateKey() method
     */
    private OAuthRsaSigner getOAuthRsaSigner(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        OAuthRsaSigner oAuthRsaSigner = new OAuthRsaSigner();
        oAuthRsaSigner.privateKey = getPrivateKey(privateKey);
        return oAuthRsaSigner;
    }

    /**
     * Creates PrivateKey from string
     *
     * @param privateKey private key in PKCS8 format
     * @return private key
     * @throws NoSuchAlgorithmException from getInstance() method
     * @throws InvalidKeySpecException from generatePrivate() method
     */
    private PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
}
