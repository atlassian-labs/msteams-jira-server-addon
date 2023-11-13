package com.microsoft.teams.utils;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.microsoft.teams.config.PluginImageSettings;
import com.microsoft.teams.config.PluginImageSettingsImpl;
import com.microsoft.teams.service.AppSettingsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.microsoft.teams.oauth.PropertiesClient.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(MockitoJUnitRunner.class)
public class ImageHelperImplTest {

    @Mock
    private AppSettingsService appSettingsService;
    private PluginImageSettings spyPluginImageSettings;

    private static final String BASE_URL = "http://localhost:8080/myjira";
    private static final String REPLACEMENT = "data:image/png";
    private final String response = getJson(BASE_URL);

    @Before
    public void setUp() {
        spyPluginImageSettings = Mockito.spy(new PluginImageSettingsImpl(appSettingsService));
    }

    private String getJson(String baseUrl) {
        return "{\"expand\":\"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations\",\"id\":\"10003\",\"self\":\"" + baseUrl + "jira/rest/api/2/issue/10003\",\"key\":\"QWE-4\",\"fields\":{\"issuetype\":{\"self\":\"" + baseUrl + "jira/rest/api/2/issuetype/10000\",\"id\":\"10000\",\"description\":\"A task that needs to be done.\",\"iconUrl\":\"" + baseUrl + "jira/secure/viewavatar?size=xsmall&avatarId=10318&avatarType=issuetype\",\"name\":\"Task\",\"subtask\":false,\"avatarId\":10318},\"components\":[],\"timespent\":null,\"timeoriginalestimate\":null,\"description\":\"h4.Searching for Information\\n{color:#707070}Use the Search bar in the top right to quickly find a particular task. \\nFor more advanced searches, click 'Search for issues' under the Issues menu. \\n{color}\\nNext: [Keyboard shortcuts|QWE-5]\\n{color:#707070}Previous:{color} [Editing tasks|QWE-3]\\n\\n----\\n[Learn more about searching|https://confluence.atlassian.com/display/JIRACORECLOUD/Searching+for+issues]\",\"project\":{\"self\":\"" + baseUrl + "jira/rest/api/2/project/10000\",\"id\":\"10000\",\"key\":\"QWE\",\"name\":\"qwe\",\"projectTypeKey\":\"business\",\"avatarUrls\":{\"48x48\":\"" + baseUrl + "jira/images/icons/priorities/medium.svg\"}},\"fixVersions\":[],\"aggregatetimespent\":null,\"resolution\":null,\"timetracking\":{},\"attachment\":[],\"aggregatetimeestimate\":null,\"resolutiondate\":null,\"workratio\":-1,\"summary\":\"Searching for information\",\"lastViewed\":null,\"watches\":{\"self\":\"" + baseUrl + "jira/rest/api/2/issue/QWE-4/watchers\",\"watchCount\":1,\"isWatching\":true},\"creator\":{\"self\":\"" + baseUrl + "jira/rest/api/2/user?username=admin\",\"name\":\"admin\",\"key\":\"admin\",\"emailAddress\":\"admin@admin.com\",\"avatarUrls\":{\"48x48\":\"" + baseUrl + "jira/secure/useravatar?ownerId=admin&avatarId=10600\",\"24x24\":\"" + baseUrl + "jira/secure/useravatar?size=small&ownerId=admin&avatarId=10600\",\"16x16\":\"" + baseUrl + "jira/secure/useravatar?size=xsmall&ownerId=admin&avatarId=10600\",\"32x32\":\"" + baseUrl + "jira/secure/useravatar?size=medium&ownerId=admin&avatarId=10600\"},\"displayName\":\"admin\",\"active\":true,\"timeZone\":\"Europe/Zaporozhye\"},\"subtasks\":[],\"created\":\"2019-10-02T16:59:54.984+0300\",\"reporter\":{\"self\":\"" + baseUrl + "jira/rest/api/2/user?username=admin\",\"name\":\"admin\",\"key\":\"admin\",\"emailAddress\":\"admin@admin.com\",\"avatarUrls\":{\"48x48\":\"" + baseUrl + "jira/secure/useravatar?ownerId=admin&avatarId=10600\",\"24x24\":\"" + baseUrl + "jira/secure/useravatar?size=small&ownerId=admin&avatarId=10600\",\"16x16\":\"" + baseUrl + "jira/secure/useravatar?size=xsmall&ownerId=admin&avatarId=10600\",\"32x32\":\"" + baseUrl + "jira/secure/useravatar?size=medium&ownerId=admin&avatarId=10600\"},\"displayName\":\"admin\",\"active\":true,\"timeZone\":\"Europe/Zaporozhye\"},\"aggregateprogress\":{\"progress\":0,\"total\":0},\"priority\":{\"self\":\"" + baseUrl + "jira/rest/api/2/priority/3\",\"iconUrl\":\"" + baseUrl + "jira/images/icons/priorities/medium.svg\",\"name\":\"Medium\",\"id\":\"3\"},\"labels\":[],\"environment\":null,\"timeestimate\":null,\"aggregatetimeoriginalestimate\":null,\"versions\":[],\"duedate\":null,\"progress\":{\"progress\":0,\"total\":0},\"comment\":{\"comments\":[{\"self\":\"" + baseUrl + "jira/rest/api/2/issue/10003/comment/10002\",\"id\":\"10002\",\"author\":{\"self\":\"" + baseUrl + "jira/rest/api/2/user?username=admin\",\"name\":\"admin\",\"key\":\"admin\",\"emailAddress\":\"admin@admin.com\",\"avatarUrls\":{\"48x48\":\"" + baseUrl + "jira/secure/useravatar?ownerId=admin&avatarId=10600\",\"24x24\":\"" + baseUrl + "jira/secure/useravatar?size=small&ownerId=admin&avatarId=10600\",\"16x16\":\"" + baseUrl + "jira/secure/useravatar?size=xsmall&ownerId=admin&avatarId=10600\",\"32x32\":\"" + baseUrl + "jira/secure/useravatar?size=medium&ownerId=admin&avatarId=10600\"},\"displayName\":\"admin\",\"active\":true,\"timeZone\":\"Europe/Zaporozhye\"},\"body\":\"121212121\",\"updateAuthor\":{\"self\":\"" + baseUrl + "jira/rest/api/2/user?username=admin\",\"name\":\"admin\",\"key\":\"admin\",\"emailAddress\":\"admin@admin.com\",\"avatarUrls\":{\"48x48\":\"" + baseUrl + "jira/secure/useravatar?ownerId=admin&avatarId=10600\",\"24x24\":\"" + baseUrl + "jira/secure/useravatar?size=small&ownerId=admin&avatarId=10600\",\"16x16\":\"" + baseUrl + "jira/secure/useravatar?size=xsmall&ownerId=admin&avatarId=10600\",\"32x32\":\"" + baseUrl + "jira/secure/useravatar?size=medium&ownerId=admin&avatarId=10600\"},\"displayName\":\"admin\",\"active\":true,\"timeZone\":\"Europe/Zaporozhye\"},\"created\":\"2019-10-03T11:23:25.329+0300\",\"updated\":\"2019-10-03T11:23:25.329+0300\"}],\"maxResults\":1,\"total\":1,\"startAt\":0},\"issuelinks\":[],\"votes\":{\"self\":\"" + baseUrl + "jira/rest/api/2/issue/QWE-4/votes\",\"votes\":0,\"hasVoted\":false},\"worklog\":{\"startAt\":0,\"maxResults\":20,\"total\":0,\"worklogs\":[]},\"assignee\":{\"self\":\"" + baseUrl + "jira/rest/api/2/user?username=admin\",\"name\":\"admin\",\"key\":\"admin\",\"emailAddress\":\"admin@admin.com\",\"avatarUrls\":{\"48x48\":\"" + baseUrl + "jira/secure/useravatar?ownerId=admin&avatarId=10600\",\"24x24\":\"" + baseUrl + "jira/secure/useravatar?size=small&ownerId=admin&avatarId=10600\",\"16x16\":\"" + baseUrl + "jira/secure/useravatar?size=xsmall&ownerId=admin&avatarId=10600\",\"32x32\":\"" + baseUrl + "jira/secure/useravatar?size=medium&ownerId=admin&avatarId=10600\"},\"displayName\":\"admin\",\"active\":true,\"timeZone\":\"Europe/Zaporozhye\"},\"updated\":\"2019-10-03T11:23:25.329+0300\",\"status\":{\"self\":\"" + baseUrl + "jira/rest/api/2/status/10000\",\"description\":\"\",\"iconUrl\":\"" + baseUrl + "jira/images/icons/status_generic.gif\",\"encodedImageUrl\":\"data:image/gif;base64,R0lGODlhEAAQALMAAHBwcHh4eLe3t+Pj44qKipycnMXFxa6uroGBgczMzKWlpf7+/gAAAAAAAAAAAAAAACH5BAEHAAsALAAAAAAQABAAAAQ3cMm5hkg0zwT60VnRAQFIKSNiTkPQYeZAjB0xgAidgjrN950fEDTrEVa6laQWVHYETSQAClBaIwA7\",\"name\":\"To Do\",\"id\":\"10000\",\"statusCategory\":{\"self\":\"" + baseUrl + "jira/rest/api/2/statuscategory/2\",\"id\":2,\"key\":\"new\",\"colorName\":\"blue-gray\",\"name\":\"To Do\"}}}}";
    }



    @Test
    public void shouldReplaceFirstImageOccurrence() {
        HttpTransport transport = getHttpTransport();
        mockSettings(true, true, true);

        String result = new ImageHelperImpl(new ImageEncoderImpl(), spyPluginImageSettings)
                .replaceImagesInJson(response, BASE_URL, transport.createRequestFactory());

        int replacedUrlsCount = getReplacedUrlsCount(REPLACEMENT, result);
        int iconsCount = 3;
        int avatarsCount = 20; // 4 avatars in project, 8 gravatars skipped
        int projectAvatarsCount = 1; // 4 avatars in project, 8 gravatars skipped

        assertEquals(iconsCount + avatarsCount + projectAvatarsCount, replacedUrlsCount);
    }

    @Test
    public void shouldReplaceFirstIconOccurrenceWhenAppropriateSetting() {
        HttpTransport transport = getHttpTransport();
        mockSettings(true, false, false);

        String result = new ImageHelperImpl(new ImageEncoderImpl(), spyPluginImageSettings)
                .replaceImagesInJson(response, BASE_URL, transport.createRequestFactory());

        int replacedUrlsCount = getReplacedUrlsCount(REPLACEMENT, result);
        int iconsCount = 3;

        assertEquals(iconsCount, replacedUrlsCount);
    }

    @Test
    public void shouldReplaceFirsAvatarOccurrenceWhenAppropriateSetting() {
        HttpTransport transport = getHttpTransport();
        mockSettings(false, false, true);

        String result = new ImageHelperImpl(new ImageEncoderImpl(), spyPluginImageSettings)
                .replaceImagesInJson(response, BASE_URL, transport.createRequestFactory());

        int replacedUrlsCount = getReplacedUrlsCount(REPLACEMENT, result);
        int avatarsCount = 20; // 4 avatars in project, 8 gravatars skipped

        assertEquals(avatarsCount, replacedUrlsCount);
    }

    @Test
    public void shouldReplaceAllAvatarsOccurrenceWhenAppropriateSetting() {
        HttpTransport transport = getHttpTransport();
        mockSettings(false, true, true);

        String result = new ImageHelperImpl(new ImageEncoderImpl(), spyPluginImageSettings)
                .replaceImagesInJson(response, BASE_URL, transport.createRequestFactory());

        int replacedUrlsCount = getReplacedUrlsCount(REPLACEMENT, result);
        int avatarsCount = 20; // 4 avatars in project, 8 gravatars skipped
        int projectAvatarsCount = 1; // 4 avatars in project, 8 gravatars skipped

        assertEquals(avatarsCount + projectAvatarsCount, replacedUrlsCount);
    }

    @Test
    public void shouldReplaceFirstProjectAvatarsOccurrenceWhenAppropriateSetting() {
        HttpTransport transport = getHttpTransport();
        mockSettings(false, true, false);

        String result = new ImageHelperImpl(new ImageEncoderImpl(), spyPluginImageSettings)
                .replaceImagesInJson(response, BASE_URL, transport.createRequestFactory());

        int replacedUrlsCount = getReplacedUrlsCount(REPLACEMENT, result);
        int projectAvatar = 1;

        assertEquals(projectAvatar, replacedUrlsCount);
    }

    private int getReplacedUrlsCount(String replacement, String result) {
        Pattern p = Pattern.compile(replacement);
        Matcher m = p.matcher(result);
        int replacedUrlsCount = 0;
        while (m.find()) {
            replacedUrlsCount++;
        }
        return replacedUrlsCount;
    }

    private HttpTransport getHttpTransport() {
        return new MockHttpTransport() {

            @Override
            public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
                return new MockLowLevelHttpRequest() {
                    @Override
                    public LowLevelHttpResponse execute() throws IOException {
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                        response.setStatusCode(200);
                        response.setContentType("image/png");
                        response.setContent("image bytes".getBytes());
                        return response;
                    }
                };
            }
        };
    }

    private void mockSettings(boolean embedIcons, boolean embedProjectAvatars, boolean embedAvatars) {
        HashMap<String, String> settings = new HashMap<>();
        settings.put(SETTINGS_EMBED_ICONS, Boolean.toString(embedIcons));
        settings.put(SETTINGS_EMBED_PROJECT_AVATARS, Boolean.toString(embedProjectAvatars));
        settings.put(SETTINGS_EMBED_AVATARS, Boolean.toString(embedAvatars));

        Mockito.when(appSettingsService.get()).thenReturn(settings);
        Mockito.when(spyPluginImageSettings.hasChanged()).thenReturn(true);
    }
}
