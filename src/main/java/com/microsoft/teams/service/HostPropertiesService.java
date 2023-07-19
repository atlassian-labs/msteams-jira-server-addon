package com.microsoft.teams.service;

import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HostPropertiesService {

    private String fullBaseUrl;
    private String contextPath;
    private String displayName;

    private static final Pattern pathPattern = Pattern.compile("(?<!\\/)\\/(?!\\/).*");

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        fullBaseUrl = applicationProperties.getBaseUrl();
        Matcher matcher = pathPattern.matcher(fullBaseUrl);
        contextPath = matcher.find() ? matcher.group() : "";
        displayName = applicationProperties.getDisplayName();
    }

    public String getBaseUrl() {
        String baseUrl;
        if (contextPath.isEmpty()) {
            baseUrl = fullBaseUrl;
        } else {
            int index = fullBaseUrl.lastIndexOf(contextPath);
            baseUrl = fullBaseUrl.substring(0, index);
        }
        return baseUrl;
    }

    public String getFullBaseUrl() {
        return fullBaseUrl;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getDisplayName() {
        return displayName;
    }

}
