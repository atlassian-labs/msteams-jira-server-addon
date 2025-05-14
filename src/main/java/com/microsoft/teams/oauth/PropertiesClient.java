package com.microsoft.teams.oauth;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.AppSettingsService;
import com.microsoft.teams.service.TeamsAtlasUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PropertiesClient {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesClient.class);

    public static final String TEAMS_ID = "teams_id";
    public static final String CONSUMER_KEY = "consumer_key";
    public static final String PRIVATE_KEY = "private_key";
    public static final String PUBLIC_KEY = "public_key";
    public static final String REQUEST_TOKEN = "request_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String USER_NAME = "user_name";
    public static final String SECRET = "secret";
    public static final String ATLAS_ID = "atlas_id";
    public static final String CONSUMER_KEY_VALUE = "OauthKey";
    public static final String TEAMS_APP_BASE_URL = "teams_app_base_url";
    public static final String SETTINGS_EMBED_ICONS = "settings_embed_icons";
    public static final String SETTINGS_EMBED_AVATARS = "settings_embed_avatars";
    public static final String SETTINGS_EMBED_PROJECT_AVATARS = "settings_embed_project_avatars";
    public static final String SETTINGS_PERSONAL_NOTIFICATIONS_CONFIGURED = "settings_personal_notifications_configured";
    public static final String SETTINGS_GROUP_NOTIFICATIONS_CONFIGURED = "settings_group_notifications_configured";
    public static final String MICROSOFT_TEAMS_INTEGRATION = "MicrosoftTeamsIntegration";
    public static final String FALSE_VALUE = "false";
    public static final String TRUE_VALUE = "true";

    private static final Map<String, String> DEFAULT_PROPERTY_VALUES = ImmutableMap.<String, String>builder()
            .put(CONSUMER_KEY, CONSUMER_KEY_VALUE)
            .put(PRIVATE_KEY, "")
            .put(PUBLIC_KEY, "")
            .put(REQUEST_TOKEN, "")
            .put(TEAMS_ID, "")
            .put(ATLAS_ID, "")
            .build();

    private static final Map<String, String> DEFAULT_SETTINGS = ImmutableMap.<String, String>builder()
            .put(SETTINGS_EMBED_ICONS, TRUE_VALUE)
            .put(SETTINGS_EMBED_AVATARS, FALSE_VALUE)
            .put(SETTINGS_EMBED_PROJECT_AVATARS, TRUE_VALUE)
            .put(SETTINGS_PERSONAL_NOTIFICATIONS_CONFIGURED, FALSE_VALUE)
            .put(SETTINGS_GROUP_NOTIFICATIONS_CONFIGURED, FALSE_VALUE)
            .build();

    private final TeamsAtlasUserService userService;
    private final AppKeysService keysService;
    private final AppSettingsService settingsService;

    @Autowired
    public PropertiesClient(TeamsAtlasUserService userService,
                            AppKeysService keysService, AppSettingsService settingsService) {
        this.userService = userService;
        this.keysService = keysService;
        this.settingsService = settingsService;
    }

    public Map<String, String> getPropertiesOrDefaults() {
        Map<String, String> map = keysService.get();
        map.putAll(Maps.difference(map, DEFAULT_PROPERTY_VALUES).entriesOnlyOnRight());
        return map;
    }

    public Map<String, String> getSettingsOrDefaults() {
        Map<String, String> map = settingsService.get();
        map.putAll(Maps.difference(map, DEFAULT_SETTINGS).entriesOnlyOnRight());
        return map;
    }

    public Map<String, String> getPropertiesFromDb(String teamsId) {
        return toMap(tryGetPropertiesFromDb(teamsId));
    }

    private Map<String, String> toMap(Properties properties) {
        return properties.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(o -> o.getKey().toString(), t -> t.getValue().toString()));
    }

    private Properties tryGetPropertiesFromDb(String teamsId) {
        Properties prop = new Properties();
        List<TeamsAtlasUser> users = userService.getUserByTeamsId(teamsId);
        if (!users.isEmpty()) {
            TeamsAtlasUser user = users.get(0);
            if (user.getAtlasAccessToken() != null) {
                prop.setProperty(ACCESS_TOKEN, user.getAtlasAccessToken());
            }
            prop.setProperty(TEAMS_ID, teamsId);
        }
        return prop;
    }

    public void saveKeysToDatabase(Map<String, String> keys) {
        keysService.add(keys);
    }

    public void saveUserToDatabase(Map<String, String> properties) {
        userService.add(properties);
    }

    public void saveSettingsToDatabase(Map<String, String> settings) {
        settingsService.set(settings);
    }

}
