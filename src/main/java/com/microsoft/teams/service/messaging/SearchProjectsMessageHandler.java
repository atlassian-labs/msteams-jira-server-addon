package com.microsoft.teams.service.messaging;

import com.atlassian.jira.util.BuildUtilsInfo;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.function.Predicate;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.teams.service.HostPropertiesService;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.models.*;
import com.microsoft.teams.utils.ImageHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

@Component
public class SearchProjectsMessageHandler implements ProcessMessageStrategy {

    private static final Logger LOG = Logger.getLogger(GetProjectsMessageHandler.class);

    private static final String SEARCH_PROJECTS_FAILED_MSG = "Cannot search projects.";

    private static final String QUERY_PARAMETER = "query";
    public static final String MIN_PROJECTS_PICKER_VERSION = "8.21.0";

    private final RequestService requestService;
    private final HostPropertiesService hostProperties;
    private final ImageHelper imageHelper;
    @ComponentImport
    private final BuildUtilsInfo buildUtilsInfo;

    @Autowired
    public SearchProjectsMessageHandler(RequestService requestService,
                                     HostPropertiesService hostProperties,
                                     ImageHelper imageHelper, BuildUtilsInfo buildUtilsInfo) {
        this.requestService = requestService;
        this.imageHelper = imageHelper;
        this.hostProperties = hostProperties;
        this.buildUtilsInfo = buildUtilsInfo;
    }

    @Override
    public String processMessage(TeamsMessage message) {
        String response;
        String teamsId = message.getTeamsId();
        Gson gson = new Gson();
        int responseCode = HttpStatus.SC_OK;
        long startTime = System.currentTimeMillis();

        try {
            RequestMessage requestMessage = (RequestMessage) message;

            String query = escapeQueueParam(URLEncodedUtils.parse(new URI(transformRequestUrl(requestMessage.getRequestUrl())), Charset.defaultCharset())
                    .stream().filter(pair -> pair.getName().equals(QUERY_PARAMETER))
                    .findFirst().get().getValue());

            // if version is lower than 8.21.0 get all projects and filter them
            if (compareVersions(buildUtilsInfo.getVersion(), MIN_PROJECTS_PICKER_VERSION) < 0) {
                response = getAndFilterProjects(requestMessage, query);
            } else {
                requestMessage.setRequestUrl(String.format("api/2/projects/picker?query=%s", URLEncoder.encode(query, Charset.defaultCharset().toString())));
                String rawProjectsPickerResponse = requestService.getAtlasData(requestMessage);

                Map projectsPickerResponseMap = gson.fromJson(rawProjectsPickerResponse, Map.class);
                int code = ((Double) projectsPickerResponseMap.get("code")).intValue();

                // get projects list by calling projects/picker endpoint only if the call was successful
                // or get all the projects and try to filter them
                if (code != HttpStatus.SC_OK) {
                    response = getAndFilterProjects(requestMessage, query);
                } else {
                    Map projectsMap = gson.fromJson(gson.toJson(projectsPickerResponseMap.get("response")), Map.class);
                    List<Project> projectsList = new ArrayList<>();

                    if (projectsMap.containsKey("projects")) {
                        List<ProjectPicker> projectsPicker = gson.fromJson(gson.toJson(projectsMap.get("projects")),
                                new TypeToken<ArrayList<ProjectPicker>>() {
                                }.getType());

                        projectsPicker.forEach(x -> {
                            Project project = new Project();
                            project.setId(x.getId());
                            project.setKey(x.getKey());
                            project.setName(x.getName());

                            project.setAvatarUrls(ImmutableMap.of("24x24", x.getAvatar()));

                            projectsList.add(project);
                        });
                    }

                    LOG.debug(String.format("Search projects. Pick %s project(s) in %s milliseconds", projectsList.size(), System.currentTimeMillis() - startTime));

                    response = gson.toJson(projectsList, new TypeToken<ArrayList<Project>>() {
                    }.getType());
                }
            }
        } catch (Exception e) {
            responseCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            response = SEARCH_PROJECTS_FAILED_MSG;
            exceptionLogExtender("Search projects. Error: ", Level.DEBUG, e);
        }

        return new ResponseMessage(imageHelper).withCode(responseCode).withResponse(response).build(
                hostProperties.getFullBaseUrl(), requestService.getHttpRequestFactory(teamsId)
        );
    }

    private String getAndFilterProjects(RequestMessage requestMessage, String filterQuery) {
        Gson gson = new Gson();
        String response;
        long startTime = System.currentTimeMillis();

        requestMessage.setRequestUrl(String.format("api/2/project"));
        String rawProjectsResponse = requestService.getAtlasData(requestMessage);

        Map projectsResponseMap = gson.fromJson(rawProjectsResponse, Map.class);
        List filteredProjects = new ArrayList();

        if (projectsResponseMap.containsKey("response")) {
            List<Project> projects = gson.fromJson(gson.toJson(projectsResponseMap.get("response")),
                    new TypeToken<ArrayList<Project>>() {
                    }.getType());

            LOG.debug(String.format("Search projects. Get all %s project(s) in %s milliseconds", projects.size(), System.currentTimeMillis() - startTime));

            Predicate<Project> byName = project -> project.getName().toLowerCase().contains(filterQuery);
            filteredProjects = projects.stream().filter(byName).collect(Collectors.toList());
        }

        LOG.debug(String.format("Search projects. Get and filter %s project(s) in %s milliseconds", filteredProjects.size(), System.currentTimeMillis() - startTime));

        response = gson.toJson(filteredProjects, new TypeToken<ArrayList<Project>>() {
        }.getType());

        return response;
    }

    private String escapeQueueParam(String queueParam) {
        try {
            if (queueParam == null) {
                return "";
            }

            return URLDecoder.decode(queueParam, Charset.defaultCharset().toString());
        } catch (Exception e) {
            return queueParam;
        }
    }

    private int compareVersions(String version1, String version2) {
        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++){
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }

    private String transformRequestUrl(String url) {
        return url.replace(" ", "%20");
    }
}
