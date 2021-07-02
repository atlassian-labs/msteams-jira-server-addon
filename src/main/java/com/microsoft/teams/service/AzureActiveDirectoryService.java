package com.microsoft.teams.service;

import com.nimbusds.jwt.JWTParser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

@Component
class AzureActiveDirectoryService {

    private static final Logger LOG = LoggerFactory.getLogger(AzureActiveDirectoryService.class);

    private String azurePublicKeyUrl;
    private String audClaim;
    private String issClaim;

    @Autowired
    AzureActiveDirectoryService(AppPropertiesService appProperties) {
        azurePublicKeyUrl = appProperties.getAzurePublicKeyUrl();
        audClaim = appProperties.getAudClaim();
        issClaim = appProperties.getIssClaim();
    }

    boolean isValidToken(String token, String teamsId) {
        return areValidClaims(token, teamsId) && hasValidSignature(token).isValid();
    }

    private TokenValidationResult validateIssuer(String token){
        TokenValidationResult result = new TokenValidationResult();
        if (!issClaim.isEmpty()) {
            String currentIssuer = getClaimValueFromToken(token, "iss");
            boolean issuerIsValid = issClaim.equals(currentIssuer);
            result.setValid(issuerIsValid);
            if (!issuerIsValid)
            {
                result.setError("Expected issuer: " + issClaim + ", got: " + currentIssuer);
            }
        } else {
            result.setValid(true);
        }
        LOG.debug("validateIssuer: " + result);
        return result;
    }

    private TokenValidationResult validateAudience(String token){
        TokenValidationResult result = new TokenValidationResult();
        String currentAudience = getClaimValueFromToken(token, "aud");
        boolean audienceIsValid = audClaim.equals(currentAudience);
        result.setValid(audienceIsValid);
        if (!audienceIsValid)
        {
            result.setError("Expected audience: " + audClaim + ", got: " + currentAudience);
        }
        LOG.debug("validateAudience: " + result);
        return result;
    }

    private TokenValidationResult validateExpiration(String token) {
        TokenValidationResult result = new TokenValidationResult();
        try {
            Date expirationTime = JWTParser.parse(token).getJWTClaimsSet().getExpirationTime();
            boolean isExpiredToken = expirationTime.before(new Date());
            result.setValid(!isExpiredToken);
            if (isExpiredToken)
            {
                result.setError("Token expired on: " + expirationTime);
            }
        } catch (Exception e) {
            result.setError(e.getClass().getSimpleName() + ":" + e.getMessage());
        }
        LOG.debug("validateExpiration: " + result);
        return result;
    }

    private TokenValidationResult validateOid(String token, String teamsId){
        TokenValidationResult result = new TokenValidationResult();
        String currentOid = getClaimValueFromToken(token, "oid");
        boolean oidIsValid = teamsId.equals(currentOid);
        result.setValid(oidIsValid);
        if (!oidIsValid)
        {
            result.setError("Expected oid: " + teamsId + ", got: " + currentOid);
        }
        LOG.debug("validateOid: " + result);
        return result;
    }

    public boolean areValidClaims(String token, String teamsId) {
        return validateAudience(token).isValid()
                && validateIssuer(token).isValid()
                && validateExpiration(token).isValid()
                && validateOid(token, teamsId).isValid();
    }

    private TokenValidationResult hasValidSignature(String token) {
        TokenValidationResult result = new TokenValidationResult();
        RestOperations restTemplate = new RestTemplate();
        String publicKeySetJson = null;

        try {
            publicKeySetJson = restTemplate.getForObject(new URI(azurePublicKeyUrl), String.class);
        } catch (Exception e) {
            result.setError(e.getClass().getSimpleName() + ":" + e.getMessage());
            exceptionLogExtender("RestOperations hasValidSignature ", Level.DEBUG, e);
            return result;
        }

        try {
            JsonWebSignature jws = new JsonWebSignature();
            JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(publicKeySetJson);
            jws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    AlgorithmIdentifiers.RSA_USING_SHA256));
            jws.setCompactSerialization(token);
            VerificationJwkSelector jwkSelector = new VerificationJwkSelector();
            JsonWebKey jwk = jwkSelector.select(jws, jsonWebKeySet.getJsonWebKeys());

            if (jwk != null) {
                RSAPublicKey rsaPublicKey = (RSAPublicKey) jwk.getKey();
                jws.setKey(rsaPublicKey);
            }

            boolean isValidSignature = jws.verifySignature();
            result.setValid(isValidSignature);

            if (!isValidSignature)
            {
                result.setError("Signature verification failed.");
            }
        } catch (Exception e) {
            exceptionLogExtender("JsonWebSignature hasValidSignature ", Level.DEBUG, e);
            result.setError(e.getClass().getSimpleName() + ":" + e.getMessage());
        }

        return result;
    }

    String getClaimValueFromToken(String token, String claimKey) {
        String claim = "";

        try {
            claim = JWTParser.parse(token).getJWTClaimsSet().getClaim(claimKey).toString()
                    .replaceAll("\\[", "").replaceAll("]", "");
        } catch (ParseException | NoSuchMethodError e) {
            exceptionLogExtender("getClaimValueFromToken ", Level.DEBUG, (Exception) e);
        }

        return claim;
    }
}
