package com.microsoft.teams.service;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Testing {@link com.microsoft.teams.service.AzureActiveDirectoryService}
 */
@RunWith(MockitoJUnitRunner.class)
public class AzureActiveDirectoryServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(AzureActiveDirectoryServiceTest.class);

    private String validJwtSample;
    private String expiredJwtSample;
    private String invalidJwtSample;
    private String validJwtSampleWithPredefinedIssAndAudClaims;
    private String teamsId;
    private AzureActiveDirectoryService azureActiveDirectoryService;
    private AppPropertiesService appProperties;

    @Before
    public void init() {
        appProperties = new AppPropertiesService();
        azureActiveDirectoryService = new AzureActiveDirectoryService(appProperties);
        teamsId = "f5d6b85d-c8d7-48a2-97d2-5ba56212c6e3";
        invalidJwtSample = "sOmE.InVaLid.Value";

        validJwtSample = generateValidJwt(teamsId, appProperties.getIssClaim(), appProperties.getAudClaim(), 120);
        expiredJwtSample = generateValidJwt(teamsId, appProperties.getIssClaim(), appProperties.getAudClaim(), -10);
        validJwtSampleWithPredefinedIssAndAudClaims = generateValidJwt(teamsId,
                "https://login.test.com/49e4d4f8-1691-4f1e-aec6-3246da86d3c6/v2.0",
                "ab3458ec-0cd0-4437-8479-735cfd3e333c",
                120);
    }

    @Test
    public void areValidClaims_IssClaimAndAudClaimFromPropertiesFile_ReturnsTrue() {
        assertTrue(azureActiveDirectoryService.areValidClaims(validJwtSample, teamsId));
    }

    @Test
    public void areValidClaims_jwtIsExpired_ReturnsFalse() {
        assertFalse(azureActiveDirectoryService.areValidClaims(expiredJwtSample, teamsId));
    }

    @Test
    public void getClaimValueFromToken_ValidTokenIssuerClaim_ReturnsCorrectIssuerUrl() {
        String issClaim = azureActiveDirectoryService.getClaimValueFromToken(validJwtSampleWithPredefinedIssAndAudClaims, "iss");
        assertEquals("https://login.test.com/49e4d4f8-1691-4f1e-aec6-3246da86d3c6/v2.0", issClaim);
    }

    @Test
    public void getClaimValueFromToken_InvalidTokenIssuerClaim_ReturnsEmptyString() {
        String issClaim = azureActiveDirectoryService.getClaimValueFromToken(invalidJwtSample, "iss");
        assertEquals("", issClaim);
    }

    @Test
    public void getClaimValueFromToken_ValidTokenAudienceClaim_ReturnsCorrectAudienceUid() {
        String audClaim = azureActiveDirectoryService.getClaimValueFromToken(validJwtSampleWithPredefinedIssAndAudClaims, "aud");
        assertEquals("ab3458ec-0cd0-4437-8479-735cfd3e333c", audClaim);
    }

    @Test
    public void isValidToken_InvalidToken_ReturnsFalse() {
        assertFalse(azureActiveDirectoryService.isValidToken(invalidJwtSample, teamsId));
    }

    private String generateValidJwt(String teamsId, String issClaim, String audClaim, long timeOffset) {
        RestOperations restTemplate = new RestTemplate();
        String jwt = "";
        try {
            NumericDate now = NumericDate.now();
            now.addSeconds(timeOffset);

            String publicKeySetJson = restTemplate.getForObject(new URI(appProperties.getAzurePublicKeyUrl()), String.class);
            JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(publicKeySetJson);
            String kid = jsonWebKeySet.getJsonWebKeys().get(0).getKeyId();

            RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
            rsaJsonWebKey.setKeyId(kid);

            JwtClaims claims = new JwtClaims();
            claims.setIssuer(issClaim);
            claims.setAudience(audClaim);
            claims.setExpirationTime(now);
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(2);
            claims.setClaim("oid", teamsId);

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setContentTypeHeaderValue("JWT");
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
            jws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    AlgorithmIdentifiers.RSA_USING_SHA256));
            jws.setKeyIdHeaderValue(kid);
            jws.setKey(rsaJsonWebKey.getPrivateKey());

            jwt = jws.getCompactSerialization();
        } catch (JoseException | URISyntaxException e) {
            LOG.debug(e.getMessage());
        }
        return jwt;
    }
}
